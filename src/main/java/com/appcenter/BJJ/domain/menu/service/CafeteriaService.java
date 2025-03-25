package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.menu.domain.Cafeteria;
import com.appcenter.BJJ.domain.menu.dto.CafeteriaInfoRes;
import com.appcenter.BJJ.domain.menu.repository.CafeteriaRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CafeteriaService {

    private final CafeteriaRepository cafeteriaRepository;

    public CafeteriaInfoRes findCafeteriaInfoByName(String name) {
        Cafeteria cafeteria = cafeteriaRepository.findFirstByName(name)
                .orElseThrow(() -> new CustomException(ErrorCode.CAFETERIA_NOT_FOUND));

        return CafeteriaInfoRes.builder()
                .name(cafeteria.getName())
                .location(cafeteria.getLocation())
                .operationTime(cafeteria.getOperationTime())
                .imageName(cafeteria.getImage())
                .build();
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
        // CAFETERIA_TB가 비어있는 경우에만 메서드 실행
        if (!cafeteriaRepository.findAll().isEmpty())
            return;

        List<Cafeteria> cafeteriaList = new ArrayList<>();

        // 방학 중
        /*cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("중식(백반)").location("11호관 (복지회관) 1층").operationTime("운영시간 (방학 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("중식(일품)").location("11호관 (복지회관) 1층").operationTime("운영시간 (방학 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("석식").location("11호관 (복지회관) 1층").operationTime("운영시간 (방학 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("국밥").location("11호관 (복지회관) 1층").operationTime("11:00~소진시").image(null).build());

        cafeteriaList.add(Cafeteria.builder()
                .name("2호관식당").corner("중식").location("2호관 (교수회관) 2층").operationTime("운영시간 (방학 중) - 평일 : 중식 10:30~13:30 석식 17:00~18:30 / 주말 : 휴점").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("2호관식당").corner("석식").location("2호관 (교수회관) 2층").operationTime("운영시간 (방학 중) - 평일 : 중식 10:30~13:30 석식 17:00~18:30 / 주말 : 휴점").image(null).build());

        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("조식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (학기 중) - 평일 : 조식 08:00~09:30 중식 11:30~13:30 / 주말 : 중식 11:00~13:00 석식 17:00~18:30").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("중식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (방학 중) - 평일 : 휴점 / 주말 : 휴점").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("석식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (학기 중) - 평일 : 조식 08:00~09:30 중식 11:30~13:30 / 주말 : 중식 11:00~13:00 석식 17:00~18:30").image(null).build());

        cafeteriaList.add(Cafeteria.builder()
                .name("27호관식당").corner("중식").location("27호관 (제2공동 실습관) 4층").operationTime("운영시간 (방학 중) - 평일 : 휴점 / 주말 : 휴점").image(null).build());

        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("중식").location("미추홀 (별관 A동) 지하 1층").operationTime("운영시간 (방학 중) - 평일 : 중식 11:00~13:30 / 주말 : 휴점").image(null).build());
        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("석식").location("미추홀 (별관 A동) 지하 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 11:00~13:30 석식 17:00~18:10 / 주말 : 휴점").image(null).build());*/


        // 학기 중
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("중식(백반)").location("11호관 (복지회관) 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("중식(일품)").location("11호관 (복지회관) 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("석식").location("11호관 (복지회관) 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("국밥").location("11호관 (복지회관) 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("4코너 (뒤쪽)").location("11호관 (복지회관) 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_student.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("학생식당").corner("5코너 (뒤쪽)").location("11호관 (복지회관) 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 10:30~14:00 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_student.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("2호관식당").corner("중식").location("2호관 (교수회관) 2층").operationTime("운영시간 (학기 중) - 평일 : 중식 11:30~13:30 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_02.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("2호관식당").corner("석식").location("2호관 (교수회관) 2층").operationTime("운영시간 (학기 중) - 평일 : 중식 11:30~13:30 석식 17:00~18:30 / 주말 : 휴점").image("cafeteria_01.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("조식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (학기 중) - 평일 : 조식 08:00~09:30 중식 11:30~13:30 / 주말 : 중식 11:00~13:00 석식 17:00~18:30").image("cafeteria_dormitory_01.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("중식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (학기 중) - 평일 : 조식 08:00~09:30 중식 11:30~13:30 / 주말 : 중식 11:00~13:00 석식 17:00~18:30").image("cafeteria_dormitory_01.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("제1기숙사식당").corner("석식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (학기 중) - 평일 : 조식 08:00~09:30 중식 11:30~13:30 / 주말 : 중식 11:00~13:00 석식 17:00~18:30").image("cafeteria_dormitory_01.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("27호관식당").corner("중식").location("27호관 (제2공동 실습관) 4층").operationTime("운영시간 (학기 중) - 평일 : 중식 11:00~13:30 / 주말 : 휴점").image("cafeteria_27.png").build());

        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("중식").location("미추홀 (별관 A동) 지하 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 11:00~13:30 석식 17:00~18:10 / 주말 : 휴점").image("cafeteria_education.png").build());
        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("석식").location("미추홀 (별관 A동) 지하 1층").operationTime("운영시간 (학기 중) - 평일 : 중식 11:00~13:30 석식 17:00~18:10 / 주말 : 휴점").image("cafeteria_education.png").build());

        cafeteriaRepository.saveAll(cafeteriaList);
    }
}
