package com.appcenter.BJJ.domain.todaydiet.service;

import com.appcenter.BJJ.domain.todaydiet.domain.Cafeteria;
import com.appcenter.BJJ.domain.todaydiet.dto.CafeteriaInfoRes;
import com.appcenter.BJJ.domain.todaydiet.repository.CafeteriaRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CafeteriaService {

    private final ObjectMapper objectMapper;
    private final CafeteriaRepository cafeteriaRepository;

    public CafeteriaInfoRes findCafeteriaInfoByName(String name) {
        Cafeteria cafeteria = cafeteriaRepository.findFirstByName(name)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFETERIA_NOT_FOUND));


        // 학기 중이면 학기 중 운영 시간, 방학 중이면 방학 중 운영 시간
        String jsonString = isDuringSemester()
                ? cafeteria.getSemesterOperationTime()
                : cafeteria.getVacationOperationTime();

        CafeteriaInfoRes.OperationTimeDto operationTimeDto = convertJsonToOperationTimeDto(jsonString);

        return CafeteriaInfoRes.builder()
                .name(cafeteria.getName())
                .location(cafeteria.getLocation())
                .operationTime(operationTimeDto)
                .imageName(cafeteria.getImage())
                .build();
    }

    private static boolean isDuringSemester() {
        // 오늘 날짜
        LocalDate today = LocalDate.now();
        int thisYear = today.getYear();

        // 1학기 시작일과 종료일
        LocalDate firstSemesterStart = LocalDate.of(thisYear, Month.MARCH, 1);
        LocalDate firstSemesterEnd = firstSemesterStart.plusWeeks(16);

        // 2학기 시작일과 종료일
        LocalDate secondSemesterStart = LocalDate.of(thisYear, Month.SEPTEMBER, 1);
        LocalDate secondSemesterEnd = secondSemesterStart.plusWeeks(16);

        // 학기 중이면 true, 방학 중이면 false
        return (today.isAfter(firstSemesterStart) && today.isBefore(firstSemesterEnd)) ||
               (today.isAfter(secondSemesterStart) && today.isBefore(secondSemesterEnd));
    }

    private CafeteriaInfoRes.OperationTimeDto convertJsonToOperationTimeDto(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, CafeteriaInfoRes.OperationTimeDto.class);
        } catch (JsonProcessingException e) {
            log.warn("[로그] 운영 시간 JSON 문자열 변환 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    public Optional<Long> findByNameAndCorner(String name, String corner) {
        Optional<Long> optionalCafeteriaId = cafeteriaRepository.findIdByNameAndCorner(name, corner);
        if (optionalCafeteriaId.isEmpty()) {
            log.info("[로그] {} {} 코너가 DB에 존재하지 않습니다.", name, corner);
        }

        return optionalCafeteriaId;
    }

    @PostConstruct
    @Transactional
    protected void insertCafeteriaInformation() {
        log.info("[로그] 식당 정보 입력 시작");

        // CAFETERIA_TB가 비어있는 경우에만 메서드 실행
        if (!cafeteriaRepository.findAll().isEmpty())
            return;

        List<Cafeteria> cafeteriaList = new ArrayList<>();

        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("중식(백반)").location("11호관 (복지회관) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("중식(일품)").location("11호관 (복지회관) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("석식").location("11호관 (복지회관) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("국밥").location("11호관 (복지회관) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("4코너(뒤쪽)").location("11호관 (복지회관) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("5코너(뒤쪽)").location("11호관 (복지회관) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}").image("cafeteria_student.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("2호관식당").corner("중식").location("2호관 (교수회관) 2층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:30~13:30\", \"석식 17:00~18:30\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 10:30~13:30\", \"석식 17:00~18:30\"], \"weekends\": [\"휴점\"]}").image("cafeteria_02.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("2호관식당").corner("석식").location("2호관 (교수회관) 2층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:30~13:30\", \"석식 17:00~18:30\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 10:30~13:30\", \"석식 17:00~18:30\"], \"weekends\": [\"휴점\"]}").image("cafeteria_01.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("조식").location("18-1호관 (제1기숙사) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"조식 08:00~09:30\", \"중식 11:30~13:30\"], \"weekends\": [\"중식 11:00~13:00\", \"석식 17:00~18:30\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"휴점\"], \"weekends\": [\"휴점\"]}").image("cafeteria_dormitory_01.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("중식").location("18-1호관 (제1기숙사) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"조식 08:00~09:30\", \"중식 11:30~13:30\"], \"weekends\": [\"중식 11:00~13:00\", \"석식 17:00~18:30\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"휴점\"], \"weekends\": [\"휴점\"]}").image("cafeteria_dormitory_01.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("석식").location("18-1호관 (제1기숙사) 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"조식 08:00~09:30\", \"중식 11:30~13:30\"], \"weekends\": [\"중식 11:00~13:00\", \"석식 17:00~18:30\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"휴점\"], \"weekends\": [\"휴점\"]}").image("cafeteria_dormitory_01.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("27호관식당").corner("A코너(중식)").location("27호관 (제2공동 실습관) 4층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"휴점\"], \"weekends\": [\"휴점\"]}").image("cafeteria_27.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("27호관식당").corner("A코너(석식)").location("27호관 (제2공동 실습관) 4층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"휴점\"], \"weekends\": [\"휴점\"]}").image("cafeteria_27.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("27호관식당").corner("B코너(중식)").location("27호관 (제2공동 실습관) 4층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"휴점\"], \"weekends\": [\"휴점\"]}").image("cafeteria_27.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("중식").location("미추홀 (별관 A동) 지하 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\"], \"weekends\": [\"휴점\"]}").image("cafeteria_education.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("국밥").location("미추홀 (별관 A동) 지하 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\"], \"weekends\": [\"휴점\"]}").image("cafeteria_education.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("석식").location("미추홀 (별관 A동) 지하 1층").semesterOperationTime("{\"operation\": \"운영시간 (학기 중)\", \"weekdays\": [\"중식 11:00~13:30\", \"석식 17:00~18:10\"], \"weekends\": [\"휴점\"]}")
                .vacationOperationTime("{\"operation\": \"운영시간 (방학 중)\", \"weekdays\": [\"중식 11:00~13:30\"], \"weekends\": [\"휴점\"]}").image("cafeteria_education.png").build());

        cafeteriaRepository.saveAll(cafeteriaList);
    }
}
