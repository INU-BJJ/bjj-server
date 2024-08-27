package com.appcenter.BJJ.service;

import com.appcenter.BJJ.domain.Cafeteria;
import com.appcenter.BJJ.repository.CafeteriaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeteriaService {

    private final CafeteriaRepository cafeteriaRepository;

    @PostConstruct
    @Transactional
    public void insertCafeteriaInformation() {
        // CAFETERIA_TB가 비어있는 경우에만 메서드 실행
        if (!cafeteriaRepository.findAll().isEmpty())
            return;

        List<Cafeteria> cafeteriaList = new ArrayList<>();

        cafeteriaList.add(Cafeteria.builder()
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
                .name("제1기숙사식당").corner("중식").location("18-1호관 (제1기숙사) 1층").operationTime("운영시간 (방학 중) - 평일 : 휴점 / 주말 : 휴점").image(null).build());

        cafeteriaList.add(Cafeteria.builder()
                .name("27호관식당").corner("중식").location("27호관 (제2공동 실습관) 4층").operationTime("운영시간 (방학 중) - 평일 : 휴점 / 주말 : 휴점").image(null).build());

        cafeteriaList.add(Cafeteria.builder()
                .name("사범대식당").corner("중식").location("미추홀 (별관 A동) 지하 1층").operationTime("운영시간 (방학 중) - 평일 : 중식 11:00~13:30 / 주말 : 휴점").image(null).build());

        cafeteriaRepository.saveAll(cafeteriaList);
    }
}
