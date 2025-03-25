package com.appcenter.BJJ.domain.todaydiet.service;

import com.appcenter.BJJ.domain.menu.domain.CafeteriaData;
import com.appcenter.BJJ.domain.menu.service.CafeteriaService;
import com.appcenter.BJJ.domain.menu.service.MenuPairService;
import com.appcenter.BJJ.domain.menu.service.MenuService;
import com.appcenter.BJJ.domain.todaydiet.domain.TodayDiet;
import com.appcenter.BJJ.domain.todaydiet.dto.DietDto;
import com.appcenter.BJJ.domain.todaydiet.dto.DietResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietUpdateService {

    private final OpenAiChatModel chatModel;
    private final TodayDietService todayDietService;
    private final CafeteriaService cafeteriaService;
    private final MenuService menuService;
    private final MenuPairService menuPairService;

    @Async
    @Retryable(
        retryFor = {
                IOException.class,              // Jsoup에서 발생하는 IO 예외
                JsonProcessingException.class,  // ObjectMapper에서 발생하는 예외 (OpenAI 응답이 잘못 되었을 가능성)
                TimeoutException.class,         // OpenAI에서 발생하는 타임아웃 예외
        },
        maxAttempts = 3,
        backoff = @Backoff(delay = 300000) // 5분 뒤 재시도
    )
    public void fetchWeeklyDietInfo() throws IOException {
        // 오늘 날짜의 식단 데이터가 이미 존재하면 추출 스킵
        if (todayDietService.checkThisWeekDietDataExist()) {
            log.info("[로그] 식단 데이터 추출 스킵, 이미 이번 주의 식단 데이터가 존재합니다.");
            return;
        }
        log.info("[로그] 이 주의 식단 데이터 추출 시작");

        // 크롤링 후 Spring AI에 질의할 JSON 데이터로 변환
        StringBuilder queryStringBuilder = new StringBuilder();
        for (CafeteriaData cafeteriaData : CafeteriaData.values()) {
            log.info("[로그] {} 크롤링 시작", cafeteriaData.getName());
            queryStringBuilder.append(executeCrawling(cafeteriaData.getName(), cafeteriaData.getUrl()));
        }
        String queryString = queryStringBuilder.toString();
        log.info("[로그] OpenAI에 질의할 JSON 데이터: \n{}", queryString);

        // OpenAI에 질의하여 식단 데이터 가져오기
        List<DietDto> dietDtos = askQuery(queryString);

        // 식단 데이터 가공 및 저장
        List<TodayDiet> todayDiets = dietDtos.stream()
                .flatMap(dietDto -> splitDietByCalories(dietDto).stream())
                .map(this::toEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        todayDietService.saveAll(todayDiets);
    }

    /*
    * 인천대학교 식당메뉴 사이트로부터 크롤링 후 JSON 데이터로 반환
    **/
    private String executeCrawling(String cafeteriaName, String cafeteriaUrl) throws IOException {
        StringBuilder queryStringBuilder = new StringBuilder();

        Document document = Jsoup.connect(cafeteriaUrl).get();

        // Class가 wrap-week-box인 div 찾기
        Element wrapWeekBox = document.getElementsByClass("wrap-week-box").first();
        if (wrapWeekBox == null) {
            log.info("class가 'wrap-week-box'인 Element를 찾을 수 없습니다.");
            return "";
        }

        Elements wrapWeeks = wrapWeekBox.select(".wrap-week");
        for (Element wrapWeek : wrapWeeks) {
            // 날짜 가져오기
            String date = wrapWeek.select(".date").text();
            LocalDate localDate = LocalDate.parse(date.replaceAll("\\(.*\\)", ""));

            // 테이블 행 가져오기
            Elements rows = wrapWeek.select("tbody tr");

            for (Element row : rows) {
                // 코너구분 (중식(백반), 중식(일품), 석식 등)
                String corner = row.select("th").text();

                corner = formattingCorner(corner);

                // 라면 코너는 스킵
                if ("라면".equals(corner)) {
                    continue;
                }

                // 해당하는 코너가 DB에 없는 경우 스킵
                Optional<Long> optionalCafeteria = cafeteriaService.findByNameAndCorner(cafeteriaName, corner);
                if (optionalCafeteria.isEmpty()) {
                    continue;
                }
                Long cafeteriaId = optionalCafeteria.get();

                // 메뉴
                String menu = row.select("td").text();
                if (menu.isEmpty() || menu.equals("오늘은 쉽니다")) {
                    continue;
                }

                queryStringBuilder.append(String.format("""
                    {
                        "date": "%s",
                        "cafeteriaId: %s,
                        "cafeteriaCorner": "%s %s",
                        "menus": "%s"
                    },
                    """, localDate, cafeteriaId, cafeteriaName, corner , menu));
            }
        }

        return queryStringBuilder.toString();
    }

    /*
    * 식당의 코너 이름에서 숫자(운영 시간) 데이터 및 공백 제거
    **/
    private String formattingCorner(String corner) {
        // 괄호 안에 숫자가 있는 경우, 괄호 포함 삭제
        String removedNumber = corner.replaceAll("\\s*\\([^)]*\\d+[^)]*\\)", "");

        // 남은 문자열에서 공백만 제거
        return removedNumber.replaceAll("\\s+", "");
    }

    /*
    * OpenAI에 크롤링 해온 식단 JSON 데이터를 파싱하도록 질의 후 DietDto 리스트로 반환
    **/
    private List<DietDto> askQuery(String query) throws JsonProcessingException {
        String jsonSchema = """
                {
                    "type": "object",
                    "required": ["diet"],
                    "properties": {
                        "diet": {
                            "type": "array",
                            "items": {
                                "type": "object",
                                "required": [
                                    "cafeteriaId",
                                    "cafeteriaCorner",
                                    "date",
                                    "menus",
                                    "price",
                                    "memberPrice",
                                    "calories",
                                    "notification"
                                ],
                                "properties": {
                                    "cafeteriaId": { "type": "integer"},
                                    "cafeteriaCorner": { "type": "string" },
                                    "date": { "type": "string" },
                                    "menus": {
                                        "type": "array",
                                        "items": { "type": "string" }
                                    },
                                    "price": { "type": "string" },
                                    "memberPrice": { "type": "string" },
                                    "calories": {
                                        "type": "array",
                                        "items": { "type": "string" }
                                    },
                                    "notification": { "type": "string" }
                                },
                                "additionalProperties": false
                            },
                            "description": "식단 리스트"
                        }
                    },
                    "additionalProperties": false
                }
                """;

        String promptMessage = """
                다음 JSON 데이터를 주어진 JSON 스키마에 맞게 변환
                        
                데이터:
                %s
                                
                변환 기준:
                각 식단에서 데이터를 다음과 같이 변환해야 함
                - `menus`:
                    - 메뉴 리스트
                    - 각 메뉴를 분리하여 리스트로 저장
                        분리 기준:
                            - 다음의 방식으로 메뉴가 구분된 경우에만 각각의 메뉴로 분리
                                - 공백
                                - 슬래시(/)
                                - or
                                - 공백이 포함된 특수문자
                            - 이외의 문자로 구분된 메뉴는 전체가 하나의 메뉴
                    - 분리 된 메뉴에 공백이 포함된 경우 공백을 제거하여 변환 (e.g., 입력: 뚝) 치즈순두부찌개, 출력: 뚝)치즈순두부찌개)
                    - 분리 된 메뉴에서 특수 문자는 제거하지 않음 (*, &, (, ) 등)
                    - 분리 된 메뉴에 추가 설명이 들어간 경우 제거하지 않음 (e.g., 우동국물(선택2구성), 나스동(돼지고기가지덮밥), 뚝)소고기버섯들깨탕)
                    - "<천원의아침밥>", "운영없음"처럼 음식이 아닌 데이터는 메뉴에 포함하지 않음
                    - 예: 8,000원
                - `price`:
                    - 가격 정보
                    - "#,###원" 형식으로 변환
                    - 예: 8,000원
                - `memberPrice`:
                    - 할인된 구성원 가격 정보
                    - "#,###원" 형식으로 변환
                    - 예: 7,000원
                - `calories`:
                    - 칼로리 리스트
                    - 각 칼로리를 분리하여 리스트로 저장
                    - "#,###kcal" 형식으로 변환
                    - 칼로리가 없는 경우:
                        - 0kcal 반환
                        - 메뉴가 or로 구분된 경우 [0kcal, 0kcal] 리스트로 반환
                    - 예: 1,524kcal
                - `notification`:
                    - 공지사항
                    - "오늘은 쉽니다"와 같은 공지사항이 있으면 해당 내용을 저장하고, 없으면 빈 문자열
                                        
                결과는 JSON 스키마를 준수해아 하며, 모든 입력값에 대해 변환해야 함
                """.formatted(query);

        Prompt prompt = new Prompt(
                promptMessage,
                OpenAiChatOptions.builder()
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                        .build()
        );
        ChatResponse response = chatModel.call(prompt);
        log.info("[로그] OpenAI에 질의한 결과: {}", response.getMetadata());

        // OpenAI 응답을 DTO 리스트로 변환
        List<DietDto> dietDtos = parseDietResponse(response.getResult().getOutput().getContent());
        log.info("[로그] OpenAI 응답을 DTO로 변환한 결과, dietDtos.size() = {}", dietDtos.size() );

        // 변환된 리스트 출력
        dietDtos.forEach(diet -> log.debug("[로그] diet: " + diet));

        return dietDtos;
    }

    /*
    * JSON 문자열 데이터를 DietResponseDto로 변환 후 DietDto 리스트를 반환
    **/
    private List<DietDto> parseDietResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        DietResponseDto responseDto = objectMapper.readValue(jsonResponse, DietResponseDto.class);
        return responseDto.getDiet();
    }

    /*
     * 칼로리가 여러 개인 경우 각각 하나의 식단으로 분리
     **/
    private List<DietDto> splitDietByCalories(DietDto dietDto) {
        List<String> calories = dietDto.getCalories();
        Queue<String> menus = dietDto.getMenus();

        if (calories.size() >= 2) {
            // 인덱스가 같은 메뉴와 칼로리를 매칭하여 저장
            List<DietDto> dietDtos = calories.stream()
                    .map(calorie -> DietDto.builder()
                            .date(dietDto.getDate())
                            .cafeteriaId(dietDto.getCafeteriaId())
                            .cafeteriaCorner(dietDto.getCafeteriaCorner())
                            .mainMenu(menus.poll())
                            .price(dietDto.getPrice())
                            .memberPrice(dietDto.getMemberPrice())
                            .calorie(calorie)
                            .notification(dietDto.getNotification())
                            .build())
                    .toList();

            // (선택n구성) 메뉴 매칭
            matchMenusToDiets(menus, dietDtos);

            return dietDtos;
        }

        return List.of(dietDto);
    }

    /*
     * 메뉴에서 괄호 숫자를 추출하여 해당하는 인덱스의 식단에 매칭
     **/
    private void matchMenusToDiets(Queue<String> menus, List<DietDto> dietDtos) {
        Pattern pattern = Pattern.compile("""
            \\(     # 여는 소괄호 찾기
            [^)]*   # 닫는 소괄호 전까지 모든 문자
            \\d+    # 적어도 하나 이상의 숫자 포함
            [^)]*   # 다시 닫는 소괄호 전까지 모든 문자
            \\)     # 닫는 소괄호 찾기
            """, Pattern.COMMENTS); // 괄호 안 숫자 찾기 ([^)]*d+[^)]*)

        while (!menus.isEmpty()) {
            String menu = menus.poll();
            Matcher matcher = pattern.matcher(menu);

            int index = -1;
            if (matcher.find()) {
                index = Integer.parseInt(matcher.group().replaceAll("\\D", "")) - 1;
            }

            String extractedMenu = matcher.replaceAll("").trim(); // 괄호 제거한 메뉴명

            // 추출한 인덱스가 유효하면 해당 DietDto에만 메뉴 추가, 그렇지 않으면 모든 DietDto에 추가
            if (index >= 0 && index < dietDtos.size()) {
                dietDtos.get(index).getMenus().add(extractedMenu);
            } else {
                dietDtos.forEach(dietDto -> dietDto.getMenus().add(extractedMenu));
            }
        }
    }

    /*
     * DietDto를 TodayDiet로 변환
     **/
    private TodayDiet toEntity(DietDto dietDto) {
        Long cafeteriaId = dietDto.getCafeteriaId();
        Queue<String> menus = dietDto.getMenus();
        String mainMenu = menus.poll();
        String subMenu = menus.isEmpty() ? "": menus.poll();
        StringBuilder restMenuBuilder = new StringBuilder();
        while (!menus.isEmpty()) {
            restMenuBuilder.append(menus.poll());

            if (!menus.isEmpty()) {
                restMenuBuilder.append(", ");
            }
        }

        // 메뉴가 존재하는지 확인하고, 없으면 새로 생성 후 id 반환
        Long mainMenuId = menuService.getOrCreateMenu(mainMenu, cafeteriaId);

        Long subMenuId = subMenu.isEmpty()
                ? null
                : menuService.getOrCreateMenu(subMenu, cafeteriaId);

        // 메뉴 쌍이 존재하는지 확인하고, 없으면 새로 생성 후 id 반환
        Long menuPairId = menuPairService.getOrCreateMenuPair(mainMenuId, subMenuId);

        return TodayDiet.builder()
                .price(dietDto.getPrice())
                .kcal(dietDto.getCalorie())
                .date(dietDto.getDate())
                .menuPairId(menuPairId)
                .restMenu(restMenuBuilder.toString())
                .build();
    }
}