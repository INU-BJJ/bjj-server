package com.appcenter.BJJ.domain.todaydiet.service;

import com.appcenter.BJJ.domain.todaydiet.domain.CafeteriaData;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DietUpdateService {

    private final ObjectMapper objectMapper;
    private final OpenAiChatModel chatModel;
    private final MenuService menuService;
    private final MenuPairService menuPairService;
    private final TodayDietService todayDietService;
    private final CafeteriaService cafeteriaService;

    // TODO: 3회 재시도 이후 실패 시 Slack 알림 및 추가 재시도 등 필요
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
    public void updateWeeklyDietWithRetry() throws IOException {
        try {
            updateWeeklyDiet();
        } catch (Exception e) {
            log.error("[로그] 식단 업데이트 중 오류 발생", e);
            throw e; // 예외를 다시 던져야 Retryable이 동작
        }
    }

    public void updateWeeklyDiet() throws IOException {
        // 오늘 날짜의 식단 데이터가 이미 존재하면 추출 스킵
        if (todayDietService.checkThisWeekDietDataExist()) {
            log.info("[로그] 식단 데이터 추출 스킵, 이미 이번 주의 식단 데이터가 존재합니다.");
            return;
        }
        log.info("[로그] 이 주의 식단 데이터 추출 시작");

        // 크롤링 후 Spring AI에 질의할 JSON 데이터로 변환
        List<String> dietJsonList = new ArrayList<>();
        for (CafeteriaData cafeteriaData : CafeteriaData.values()) {
            log.info("[로그] {} 크롤링 시작", cafeteriaData.getName());
            dietJsonList.addAll(crawlWeeklyDietData(cafeteriaData.getName(), cafeteriaData.getUrl()));
        }
        String dietJsonData = dietJsonList.toString();
        log.info("[로그] OpenAI에 질의할 JSON 데이터 개수: {}, 데이터: \n{}", dietJsonList.size(), dietJsonData);

        // OpenAI에 질의하여 식단 데이터 가져오기
        List<DietDto> dietDtos = convertDietDataWithAI(dietJsonData);

        // 식단 데이터 가공 및 저장
        List<TodayDiet> todayDiets = dietDtos.stream()
                .flatMap(dietDto -> cleanAndSplitDiets(dietDto).stream())
                .map(this::buildTodayDietWithMenus)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        todayDietService.saveAll(todayDiets);
    }

    /*
    * 인천대학교 식당메뉴 사이트로부터 크롤링 후 JSON 데이터로 반환
    **/
    private List<String> crawlWeeklyDietData(String cafeteriaName, String cafeteriaUrl) throws IOException {
        List<String> dietJsonList = new ArrayList<>();

        Document document = Jsoup.connect(cafeteriaUrl)
                .timeout(10_000) // 10초 타임아웃 설정
                .get();

        // Class가 wrap-week-box인 div 찾기
        Element wrapWeekBox = document.getElementsByClass("wrap-week-box").first();
        if (wrapWeekBox == null) {
            log.info("class가 'wrap-week-box'인 Element를 찾을 수 없습니다.");
            return Collections.emptyList();
        }

        Elements wrapWeeks = wrapWeekBox.select(".wrap-week");
        for (int i = 0; i < wrapWeeks.size(); i++) {
            // 날짜 가져오기
            LocalDate localDate = LocalDate.now().with(DayOfWeek.MONDAY).plusDays(i);

            dietJsonList.addAll(processDailyDiet(wrapWeeks.get(i), localDate, cafeteriaName));
        }

        return dietJsonList;
    }

    /*
    * 주별 데이터를 처리하여 JSON 문자열 생성
    **/
    private List<String> processDailyDiet(Element wrapWeek, LocalDate localDate, String cafeteriaName) {
        List<String> dietJsonList = new ArrayList<>();

        // 테이블 행 가져오기
        Elements rows = wrapWeek.select("tbody tr");

        for (Element row : rows) {
            // 코너구분 (중식(백반), 중식(일품), 석식 등)
            String corner = cleanCornerText(row.select("th").text());

            // 라면 코너는 스킵
            if ("라면".equals(corner)) {
                continue;
            }

            // 해당하는 코너가 DB에 없는 경우 스킵
            Long cafeteriaId = cafeteriaService.findByNameAndCorner(cafeteriaName, corner).orElse(null);
            if (cafeteriaId == null) {
                continue;
            }

            // 메뉴가 없거나 '오늘은 쉽니다'인 경우 스킵
            String diet = row.select("td").text();
            if (diet.isEmpty() || diet.equals("오늘은 쉽니다")) {
                continue;
            }

            dietJsonList.addAll(appendDietJson(localDate, cafeteriaId, cafeteriaName, corner, diet));
        }

        return dietJsonList;
    }

    /*
    * 메뉴 데이터를 JSON 형식으로 추가
    **/
    private List<String> appendDietJson(LocalDate localDate, Long cafeteriaId, String cafeteriaName, String corner, String diet) {
        List<String> dietJsonList = new ArrayList<>();

        // "-" 3개 이상을 기준으로 식단 분리
        String[] dietBlocks = diet.split("-{3,}");

        // JSON 포맷 문자열 정의
        String jsonFormat = """
            {
                "date": "%s",
                "cafeteriaId": %s,
                "cafeteriaCorner": "%s %s",
                "diet": "%s"
            },
            """;

        // 첫 번째 식단 추가
        dietJsonList.add(String.format(jsonFormat, localDate, cafeteriaId, cafeteriaName, corner, dietBlocks[0]));

        // 두 번째 블록부터 "국밥"이 포함된 식단 추가
        for (int j = 1; j < dietBlocks.length; j++) {
            if (dietBlocks[j].contains("국밥")) {
                // "*국밥*" 패턴을 제거
                String cleanedMenu = dietBlocks[j].replaceAll("\\*국밥\\*", "").trim();
                if (!cleanedMenu.isEmpty()) {
                    dietJsonList.add(String.format(jsonFormat, localDate, cafeteriaId, cafeteriaName, corner, cleanedMenu));
                }
                break;  // 국밥 식단은 하나만 추가
            }
        }

        return dietJsonList;
    }

    /*
    * 식당의 코너 이름에서 운영 시간 데이터 및 공백 제거
    **/
    private String cleanCornerText(String corner) {
        if (corner == null || corner.isBlank()) {
            return "";
        }

        // 모든 공백 제거
        corner = corner.replaceAll("\\s+", "");

        // (00:00~????) 형식 삭제
        corner = corner.replaceAll("\\(?\\d{2}:\\d{2}~.*\\)?", "");
        // 시간대 정보와 괄호를 포함한 패턴 제거
        corner = corner.replaceAll("""
            (?x)                # Verbose mode 활성화
            \\(?                # 여는 괄호 '('가 있을 수도 있고 없을 수도 있음
            \\d{2}:\\d{2}       # 시간 시작 (예: 10:30)
            ~                   # 틸드 구분자
            .*                  # 그 이후 아무 문자열
            \\)?                # 닫는 괄호 ')'가 있을 수도 있고 없을 수도 있음
            """, "");

        // '평일', '주말' 키워드 제거
        corner = corner.replaceAll("평일|주말", "");

        return corner;
    }

    /*
    * OpenAI에 크롤링 해온 식단 JSON 데이터를 파싱하도록 질의 후 DietDto 리스트로 반환
    **/
    private List<DietDto> convertDietDataWithAI(String dietJsonData) throws JsonProcessingException {
        String dietJsonSchema = """
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
                                    "prices",
                                    "memberPrices",
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
                                    "prices": {
                                        "type": "array",
                                        "items": { "type": "string" }
                                    },
                                    "memberPrices": {
                                        "type": "array",
                                        "items": { "type": "string" }
                                    },
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
                    - 분리 된 메뉴에서 다음 특수 문자는 제거하지 않음 (*, &, (, ))
                    - 분리 된 메뉴에 추가 설명이 들어간 경우 제거하지 않음 (e.g., 우동국물(선택2구성), 나스동(돼지고기가지덮밥), 소고기버섯들깨탕)
                    - "<천원의아침밥>", "운영없음"처럼 음식이 아닌 데이터는 메뉴에 포함하지 않음
                    - 예: 8,000원
                - `prices`:
                    - 가격 리스트
                    - 각 가격을 분리하여 리스트로 저장
                    - "#,###원" 형식으로 변환
                    - 예: 8,000원
                - `memberPrices`:
                    - 할인된 구성원 가격 리스트
                    - 각 구성원 가격을 분리하여 리스트로 저장
                    - "#,###원" 형식으로 변환
                    - 예: 7,000원
                - `calories`:
                    - 칼로리 리스트
                    - 각 칼로리를 분리하여 리스트로 저장
                    - "#,###kcal" 형식으로 변환
                    - 칼로리가 없는 경우:
                        - 가격 또는 구성원 가격의 수에 맞게 0kcal 반환
                        - 메뉴가 or로 구분된 경우 or 개수에 맞게 [0kcal, 0kcal, ...] 형태의 리스트로 반환
                    - 예: 1,524kcal
                - `notification`:
                    - 공지사항
                    - "오늘은 쉽니다"와 같은 공지사항이 있으면 해당 내용을 저장하고, 없으면 빈 문자열
                                        
                결과는 JSON 스키마를 준수해아 하며, 모든 입력값에 대해 변환해야 함
                """.formatted(dietJsonData);

        Prompt prompt = new Prompt(
                promptMessage,
                OpenAiChatOptions.builder()
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, dietJsonSchema))
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
        objectMapper.registerModule(new JavaTimeModule());
        DietResponseDto responseDto = objectMapper.readValue(jsonResponse, DietResponseDto.class);
        return responseDto.getDiet();
    }

    /*
     * 칼로리 또는 가격, 구성원 가격이 여러 개인 경우 각각 하나의 식단으로 분리,
     * 추가로 '<천원의아침밥>'이라는 문자열이 첫 번째 메뉴에 있는 경우 삭제
     **/
    private List<DietDto> cleanAndSplitDiets(DietDto dietDto) {
        Queue<String> menus = dietDto.getMenus();

        // 모든 메뉴 정제 (슬래시 기준 분리 -> '천원의아침밥' 제거 → 수식어 제거 -> 특수문자 제거)
        List<String> cleanedMenus = menus.stream()
                .flatMap(menu -> Arrays.stream(menu.split("/"))
                        .map(String::trim)) // 슬래시로 분리 후 앞뒤 공백 제거
                .map(this::cleanMenuText)
                .filter(menu -> !menu.isEmpty()) // 빈 문자열 제거
                .toList();

        // "운영없음"이 포함된 경우 아예 이 식단 자체를 건너뛰기
        if (cleanedMenus.contains("운영없음")) {
            return Collections.emptyList();
        }

        // 정제된 메뉴를 새로운 Deque로 재구성
        menus = new ArrayDeque<>(cleanedMenus);
        dietDto.updateMenus(menus);

        // 칼로리, 가격, 구성원 가격 중 가장 긴 리스트의 크기를 구함
        int maxSize = Stream.of(
                dietDto.getCalories().size(),
                dietDto.getPrices().size(),
                dietDto.getMemberPrices().size()
        ).max(Integer::compare).orElse(0);

        // 칼로리와 가격, 구성원 가격 각각이 모두 하나인 경우 리스트로 감싸서 바로 반환
        if (maxSize < 2) {
            return List.of(dietDto);
        }

        // 인덱스가 같은 메뉴와 칼로리, 가격, 구성원 가격을 매칭하여 저장
        List<DietDto> dietDtos = IntStream.range(0, maxSize)
                .mapToObj(i -> DietDto.builder()
                        .date(dietDto.getDate())
                        .cafeteriaId(dietDto.getCafeteriaId())
                        .cafeteriaCorner(dietDto.getCafeteriaCorner())
                        .mainMenu(dietDto.pollMenu())
                        .price(dietDto.getPrice(i))
                        .memberPrice(dietDto.getMemberPrice(i))
                        .calorie(dietDto.getCalorie(i))
                        .notification(dietDto.getNotification())
                        .build())
                .toList();

        // (선택n구성) 메뉴 매칭
        matchMenusToDiets(menus, dietDtos);

        return dietDtos;
    }


    /*
     * '천원의아침밥'이라는 문자열이 메뉴에 있는 경우 해당 문자열을 제거,
     * 메뉴 앞에 붙은 수식어 제거 (ex. 'New)', '만우절)', '뚝)', '(뚝)')
     * *, &, (, )을 제외한 모든 특수문자 제거
     * 메뉴에 한글이 포함되어있는지 확인하고, 한글이 없다면 빈 문자열 반환
     **/
    private String cleanMenuText(String menu) {
        if (menu == null) return "";

        // '<천원의아침밥>' 제거 (앞뒤 꺽쇠 없어도 제거 가능)
        menu = menu.replaceAll("<?천원의아침밥>?", "").trim();

        // 메뉴 앞에 붙은 수식어 제거 (ex. 'New)', '만우절)', '뚝)', '(뚝)')
        menu = menu.replaceAll("""
            (?x)    # Verbose Mode (주석 허용) 활성화
            ^       # 문자열의 시작
            [^)]+   # 닫는 괄호 ')'를 제외한 모든 문자 (최소 1개 이상)
            \\)     # 닫는 괄호 ')'
            """, "").trim();

            // *, &, (, )을 제외한 모든 특수문자 제거
            menu = menu.replaceAll("[^a-zA-Z0-9가-힣*&()]", "");

        // 메뉴에 한글이 포함되어있는지 확인하고, 한글이 없다면 빈 문자열 반환
        if (!menu.matches(".*[가-힣].*")) {
            return "";
        }

        return menu;
    }

    /*
     * 메뉴에서 괄호 숫자를 추출하여 해당하는 인덱스의 식단에 매칭
     **/
    private void matchMenusToDiets(Queue<String> menus, List<DietDto> dietDtos) {
        Pattern pattern = Pattern.compile("""
            (?x)    # Verbose Mode (주석 허용) 활성화
            \\(     # 여는 소괄호 '(' 찾기
            [^)]*   # 닫는 소괄호 ')' 전까지 모든 문자 (0개 이상)
            \\d+    # 적어도 하나 이상의 숫자 포함
            [^)]*   # 다시 닫는 소괄호 ')' 전까지 모든 문자 (0개 이상)
            \\)     # 닫는 소괄호 ')' 찾기
            """);   // 괄호 안 숫자 찾기 ([^)]*d+[^)]*)

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
     * DietDto로 TodayDiet 객체 생성. 이 때 기존 Menu 및 MenuPair가 없으면 생성 및 저장
     **/
    private TodayDiet buildTodayDietWithMenus(DietDto dietDto) {
        Long cafeteriaId = dietDto.getCafeteriaId();
        Queue<String> menus = dietDto.getMenus();

        String mainMenu = dietDto.pollMenu();
        String subMenu = dietDto.pollMenu();
        String restMenu = String.join(", ", menus);

        // 메인 메뉴가 없는 경우 객체 생성 스킵
        if (mainMenu.isEmpty())
            return null;

        // 메뉴가 존재하는지 확인하고, 없으면 새로 생성 후 id 반환
        Long mainMenuId = menuService.getOrCreateMenu(mainMenu, cafeteriaId);
        Long subMenuId = subMenu.isEmpty()
                ? null
                : menuService.getOrCreateMenu(subMenu, cafeteriaId);

        // 메뉴 쌍이 존재하는지 확인하고, 없으면 새로 생성 후 id 반환
        Long menuPairId = menuPairService.getOrCreateMenuPair(mainMenuId, subMenuId);

        return TodayDiet.builder()
                .price(dietDto.getPrice(0))
                .kcal(dietDto.getCalorie(0))
                .date(dietDto.getDate())
                .menuPairId(menuPairId)
                .restMenu(restMenu)
                .build();
    }
}