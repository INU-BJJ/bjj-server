package com.appcenter.BJJ.domain.todaydiet.service;

import com.appcenter.BJJ.domain.image.Image;
import com.appcenter.BJJ.domain.image.ImageRepository;
import com.appcenter.BJJ.domain.review.utils.ReviewPolicy;
import com.appcenter.BJJ.domain.todaydiet.domain.TodayDiet;
import com.appcenter.BJJ.domain.todaydiet.dto.TodayDietRes;
import com.appcenter.BJJ.domain.todaydiet.dto.TodayMenuRes;
import com.appcenter.BJJ.domain.todaydiet.repository.TodayDietRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TodayDietService {

    private final ImageRepository imageRepository;
    private final TodayDietRepository todayDietRepository;
    private final ReviewPolicy reviewPolicy;

    public List<TodayDietRes> findByCafeteria(String cafeteriaName, long memberId) {
        log.info("[로그] findByCafeteria() 시작, cafeteriaName: {}, memberId: {}", cafeteriaName, memberId);

        List<TodayDietRes> todayDietResList = todayDietRepository.findTodayDietsByCafeteriaName(cafeteriaName, memberId);
        log.info("[로그] todayDietResList.size() : {}", todayDietResList.size());

        todayDietResList.forEach(todayDietRes -> {
            Image image = imageRepository.findFirstImageOfMostLikedReview(todayDietRes.getMenuPairId());

            if (image != null) {
                todayDietRes.setReviewImageName(image.getName());
            }
            log.info("[로그] todayDietRes.getReviewImageName() : {}", todayDietRes.getReviewImageName());
        });

        return todayDietResList;
    }

    public List<TodayMenuRes> findMainMenusByCafeteria(String cafeteriaName) {
        log.info("[로그] findMainMenusByCafeteria() 시작");

        List<TodayMenuRes> todayMenuResList = todayDietRepository.findTodayMainMenusByCafeteriaName(cafeteriaName);
        LocalTime now = LocalTime.now();
        log.info("[로그] todayMenuResList.size() : {}, now : {}", todayMenuResList.size(), now);

        return todayMenuResList.stream()
                .filter(todayMenuRes -> reviewPolicy.isReviewableTime(todayMenuRes.getCafeteriaCorner(), LocalTime.now()))
                .toList();
    }

    public boolean checkThisWeekDietDataExist() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);    // 이번 주 월요일 날짜

        return todayDietRepository.existsByStartDate(monday, Limit.of(1));
    }

    @Transactional
    public void saveAll(List<TodayDiet> todayDietList) {
        todayDietRepository.saveAll(todayDietList);
    }
}
