package com.appcenter.BJJ.domain.todaydiet.service;

import com.appcenter.BJJ.domain.image.ImageDto;
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
import java.util.Map;
import java.util.stream.Collectors;

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

        List<ImageDto> images = imageRepository.findFirstImagesOfMostLikedReviewInMenuPairIds(todayDietResList.stream().map(TodayDietRes::getMenuPairId).toList());
        Map<Long, String> imageMap = images.stream()
                .collect(Collectors.toMap(
                        ImageDto::getMenuId,
                        ImageDto::getImageName
                ));

        for(TodayDietRes diet : todayDietResList) {
            diet.setReviewImageName(imageMap.get(diet.getMenuPairId()));
        }

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
