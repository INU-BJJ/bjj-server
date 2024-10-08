package com.appcenter.BJJ.service;

import com.appcenter.BJJ.domain.*;
import com.appcenter.BJJ.dto.TodayDietRes;
import com.appcenter.BJJ.dto.TodayMenuRes;
import com.appcenter.BJJ.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TodayDietService {

    private final CafeteriaRepository cafeteriaRepository;
    private final ImageRepository imageRepository;
    private final MenuRepository menuRepository;
    private final MenuPairRepository menuPairRepository;
    private final TodayDietRepository todayDietRepository;

    public List<TodayDietRes> findByCafeteria(String cafeteriaName) {

        List<TodayDietRes> todayDietResList = todayDietRepository.findTodayDietsByCafeteriaName(cafeteriaName);

        todayDietResList.forEach(todayDietRes -> {
            Image image = imageRepository.findFirstImageOfMostLikedReview(todayDietRes.getMenuPairId(), Limit.of(1));

            if (image != null) {
                todayDietRes.setReviewImagePath(image.getPath());
            }
        });

        return todayDietResList;
    }

    public List<TodayMenuRes> findMainMenusByCafeteria(String cafeteriaName) {

        List<TodayMenuRes> todayMenuResList = todayDietRepository.findTodayMainMenusByCafeteriaName(cafeteriaName);
        LocalTime now = LocalTime.now();

        return todayMenuResList.stream().filter(todayMenuRes -> {
            if (now.isBefore(LocalTime.of(8, 0))){
                return false;
            } else if (now.isBefore(LocalTime.of(10, 30))) {
                return todayMenuRes.getCafeteriaCorner().contains("조식");
            } else if (now.isBefore(LocalTime.of(17, 0))) {
                return !todayMenuRes.getCafeteriaCorner().contains("석식");
            } else {
                return true;
            }
        }).toList();
    }

    @PostConstruct  // bean 생성 후 실행
    @Scheduled(cron = "0 0 7 * * *")
    // 자정마다 실행 (초 분 시 일 월 요일 순서, 0 0 0/1 * * * -> 1시간 마다 실행, 0 0 7 * * * -> 7시에 실행)
    // 스케쥴링은 프록시 메소드가 적어도 protected 이어야 함
    protected void crawlDailyMenus() throws IOException {

        /* 오늘 날짜 00/00 형식으로 저장 */
        LocalDate localDate = LocalDate.now();
        if (todayDietRepository.existsByDate(localDate)) {
            log.info("[로그] 크롤링 스킵, 이미 {} 날짜의 데이터가 존재합니다.", localDate);
            return;
        }
        String today = String.format("%02d", localDate.getMonthValue()) + "/" + String.format("%02d", localDate.getDayOfMonth());
        log.info("[로그] 오늘 날짜 가져오기, today = {}", today);

        for (CafeteriaData cafeteriaData : CafeteriaData.values()) {
            log.info("[로그] {} 크롤링 시작", cafeteriaData.getName());
            executeCrawling(cafeteriaData.getName(), cafeteriaData.getUrl(), today);
        }
    }

    @Transactional
    public void executeCrawling(String cafeteriaName, String cafeteriaUrl, String today) throws IOException {
        /* ID가 menuBox인 테이블 찾기 */
        Document document = Jsoup.connect(cafeteriaUrl).get();
        Element table = document.getElementById("menuBox");
        if (table == null) {
            log.info("[로그] ID가 'menuBox'인 테이블을 찾을 수 없습니다.");
            return;
        }
        Elements rows = table.select("tr");

        /* 테이블에서 오늘 날짜의 td 인덱스 검색 */
        int todayIndex = -1;
        Element row = rows.get(0);
        Elements columns = row.select("td");
        for (int index = 0; index < columns.size(); index++) {
            Element column = columns.get(index);
            if (column.text().contains(today)) todayIndex = index;
        }
        log.info("[로그] 오늘 날짜의 td 인덱스 검색, todayIndex = {} ", todayIndex);
        if (todayIndex == -1) {
            log.info("[로그] 오늘 날짜의 td 인덱스를 찾을 수 없습니다.");
            return;
        }

        for (int i = 1; i < rows.size(); i++) {
            row = rows.get(i);
            Element column = row.select("td").get(todayIndex);
            String text = column.text();
            if (text.isEmpty())
                continue;
            String cornerName = row.select("td").get(0).text();
            log.info("[로그] 코너별 텍스트 추출, 코너 = {}, text = {}", cornerName, text);

            Optional<Cafeteria> optionalCafeteria = cafeteriaRepository.findByNameAndCorner(cafeteriaName, cornerName);
            if (optionalCafeteria.isEmpty()) {
                log.info("[로그] {} {} 코너가 DB에 존재하지 않습니다.", cafeteriaName, cornerName);
                continue;
            }
            Long cafeteriaId = optionalCafeteria.get().getId();
            /*Long cafeteriaId = 1L;*/

            // "오늘 등록된 메뉴가 없습니다." 처리
            if (text.contains("오늘 등록된 메뉴가 없습니다.")) {
                log.info("[로그] 오늘 등록된 메뉴가 없습니다.");
                return; // 추가 처리를 하지 않도록 종료
            }

            // 추가로 코너별로 분리
            String[] corners = text.split("\\s*(?=\\[\\d코너])");

            for (String corner : corners) {
                if (!corner.trim().isEmpty()) {
                    parseText(corner.trim(), cafeteriaId);
                }
            }
        }
    }

    private void parseText(String text, Long cafeteriaId) {
        // 특수 문자 및 구분 기호 제거
        text = text.replaceAll("-{2,}", "").trim();
        text = text.replaceAll("\"", "").trim();

        // 기본 값 초기화
        String menuText = "";
        String price = "";
        String memberPrice = "";
        String kcal = "";
        String selfMenu = "";
        String operationTime = "";

        // 메뉴와 가격, 구성원 가격, 칼로리 추출
        Pattern menuDetailsPattern = Pattern.compile("(.*?)\\s.?(\\d+.*\\d)원?\\s*\\(구성원\\s*(\\d+.*\\d)원?\\)\\s*(\\d+,?\\d+kcal)");
        Matcher menuDetailsMatcher = menuDetailsPattern.matcher(text);
        if (menuDetailsMatcher.find()) {
            menuText = menuDetailsMatcher.group(1).trim();
            price = menuDetailsMatcher.group(2).trim() + "원";
            memberPrice = menuDetailsMatcher.group(3).trim() + "원";
            kcal = menuDetailsMatcher.group(4).trim();
            text = text.replace(menuDetailsMatcher.group(0), "").trim(); // 해당 정보를 제거
        }

        // 셀프 메뉴 추출
        Pattern selfMenuPattern = Pattern.compile("[^s]?셀프[^\\s)]*[\\s)](.*?)(?=.?운영시간|$)");
        Matcher selfMenuMatcher = selfMenuPattern.matcher(text);
        if (selfMenuMatcher.find()) {
            selfMenu = selfMenuMatcher.group(1).trim();
            text = text.replace(selfMenuMatcher.group(0), "").trim(); // 해당 정보를 제거
            //selfMenu = selfMenu.replace("")
        }

        // 운영 시간 추출
        Pattern timePattern = Pattern.compile("\\*?운영시간[^0-9]*(\\d{1,2}:\\d{2}~\\d{1,2}:\\d{2})");
        Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            operationTime = timeMatcher.group(1).trim();
            text = text.replace(timeMatcher.group(0), "").trim(); // 해당 정보를 제거
        }

        // 나머지 텍스트는 메뉴로 간주
//        menuText = (menuText + " " + text).trim();

        // 출력
        log.info("[로그] 추출된 값 출력" +
                "\n메뉴: " + menuText +
                "\n가격: " + price +
                "\n구성원 가격: " + memberPrice +
                "\n칼로리: " + kcal +
                "\n셀프 메뉴: " + selfMenu +
                "\n운영 시간: " + operationTime);

        // 추출 실패 시 종료
        if (Objects.equals(menuText, "") || Objects.equals(price, "") || Objects.equals(kcal, ""))
            return;

        TodayDiet todayDiet = TodayDiet.builder()
                .price(price)
                .kcal(kcal)
                .date(LocalDate.now())
                .build();

        String[] menuArray = menuText.split(" ", 3);

        Long mainMenuId = menuRepository.findFirstByMenuNameAndCafeteriaId(menuArray[0], cafeteriaId)
                .orElseGet(() ->
                        menuRepository.save(
                                Menu.builder()
                                        .menuName(menuArray[0])
                                        .cafeteriaId(cafeteriaId)
                                        .build()
                        )
                ).getId();
        Long subMenuId;
        String restMenu = null;

        if (menuArray.length != 1) {
            Menu subMenu = menuRepository.findFirstByMenuNameAndCafeteriaId(menuArray[1], cafeteriaId)
                    .orElseGet(() ->
                            menuRepository.save(
                                    Menu.builder()
                                            .menuName(menuArray[1])
                                            .cafeteriaId(cafeteriaId)
                                            .build()
                            )
                    );

            subMenuId = subMenu.getId();
            if (menuArray.length == 3) {
                restMenu = subMenu.getMenuName() + " " + menuArray[2];
            }
        } else {
            subMenuId = null;
        }

        MenuPair menuPair = menuPairRepository.findFirstByMainMenuIdAndSubMenuId(mainMenuId, subMenuId)
                .orElseGet(() ->
                        menuPairRepository.save(
                                MenuPair.builder()
                                        .mainMenuId(mainMenuId)
                                        .subMenuId(subMenuId)
                                        .build()
                        )
                );

        todayDiet.determineMenu(menuPair.getId(), restMenu);
        todayDietRepository.save(todayDiet);
    }
}
