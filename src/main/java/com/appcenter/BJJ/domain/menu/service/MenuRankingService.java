package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.image.ImageDto;
import com.appcenter.BJJ.domain.image.ImageRepository;
import com.appcenter.BJJ.domain.menu.domain.MenuRanking;
import com.appcenter.BJJ.domain.menu.dto.*;
import com.appcenter.BJJ.domain.menu.repository.MenuLikeRepository;
import com.appcenter.BJJ.domain.menu.repository.MenuRankingRepository;
import com.appcenter.BJJ.domain.menu.repository.MenuRepository;
import com.appcenter.BJJ.domain.menu.repository.TodayDietRepository;
import com.appcenter.BJJ.domain.review.dto.BestReviewDto;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuRankingService {

    private final MenuRankingRepository menuRankingRepository;
    private final TodayDietRepository todayDietRepository;
    private final ReviewRepository reviewRepository;
    private final MenuLikeRepository menuLikeRepository;
    private final MenuRepository menuRepository;
    private final ImageRepository imageRepository;

    public MenuRankingPagedRes getMenuRanking(int pageNumber, int pageSize) {
        log.info("[로그] getMenuRanking() 시작, pageNumber: {}, pageSize: {}", pageNumber, pageSize);

        // 오늘 날짜 및 학기 구하기
        LocalDate now = LocalDate.now();
        int semester = getSemester(now);

        // 최소 리뷰 개수를 충족한 메뉴 랭킹 리스트를 최대 maxMenuRank개 가져오기
        int minRatingCount = 5; // 최소 리뷰 개수
        int maxMenuRank = 30;   // 랭킹 최대 값
        if (pageNumber * pageSize + pageSize > maxMenuRank) {
            pageSize = maxMenuRank - pageNumber * pageSize;
        }
        Slice<MenuRanking> menuRankingSlice = (pageSize > 0)
                ? menuRankingRepository.findBySemesterAndRatingCountOrderByRating(semester, minRatingCount, PageRequest.of(pageNumber, pageSize))
                : new SliceImpl<>(Collections.emptyList());
        log.debug("[로그] menuRankingList = {}", menuRankingSlice.getContent());

        // 각 메뉴별 가장 좋아요를 많이 받은 리뷰 가져오기
        List<Long> menuIdList = menuRankingSlice.stream()
                .map(MenuRanking::getMenuId).toList();
        Map<Long, BestReviewDto> bestReviewMap = reviewRepository.findMostLikedReviewIdsInMainMenuIds(menuIdList).stream()
                .collect(Collectors.toMap(BestReviewDto::getMenuId, Function.identity()));
        log.debug("[로그] bestReviewMap = {}", bestReviewMap);

        List<MenuRankingDetailRes> menuRankingDetailList = menuRankingSlice.stream().map(menuRanking -> {

            BestReviewDto bestReviewDto = bestReviewMap.get(menuRanking.getMenuId());
            Long bestReviewId = null;
            if (bestReviewDto == null) {
                log.warn("[로그] menuId가 {}인 BestReviewDto가 존재하지 않습니다", menuRanking.getMenuId());
            } else {
                bestReviewId = bestReviewDto.getBestReviewId();
            }

            return MenuRankingDetailRes.builder()
                    .menuId(menuRanking.getMenuId())
                    .menuName(menuRanking.getMenuName())
                    .menuRating(menuRanking.getMenuRating())
                    .cafeteriaName(menuRanking.getCafeteriaName())
                    .cafeteriaCorner(menuRanking.getCafeteriaCorner())
                    .bestReviewId(bestReviewId)
                    .updatedAt(menuRanking.getUpdatedAt())
                    .build();
        }).toList();

        // 1, 2, 3등 메뉴의 가장 좋아요를 많이 받은 리뷰 이미지 가져오기
        int startIndex = pageNumber * pageSize;
        if (startIndex < 3) {
            int endIndex = Math.min(3 - startIndex, menuIdList.size());
            List<Long> bestThreeMenuIds = menuIdList.subList(0, endIndex);
            Map<Long, ImageDto> imageMap = imageRepository.findFirstImagesOfMostLikedReviewInMainMenuIds(bestThreeMenuIds).stream()
                    .collect(Collectors.toMap(ImageDto::getMenuId, Function.identity()));

            String imageMapLog = imageMap.entrySet().stream()
                    .map(entry -> String.format("%s=ImageDto(menuId=%d, imageName=%s)", entry.getKey(), entry.getValue().getMenuId(), entry.getValue().getImageName()))
                    .collect(Collectors.joining(", "));
            log.info("[로그] imageDto = {{}}", imageMapLog);

            for (int i = 0; i < endIndex; i++) {
                MenuRankingDetailRes menuRankingDetailRes = menuRankingDetailList.get(i);
                menuRankingDetailRes.initReviewImageName(imageMap.get(menuRankingDetailRes.getMenuId()).getImageName());
            }
        }

        return MenuRankingPagedRes.builder()
                .menuRankingDetailList(menuRankingDetailList)
                .isLastPage(menuRankingSlice.isLast())
                .build();
    }


    @PostConstruct  // bean 생성 후 실행
    @Transactional
    @Scheduled(cron = "0 0 4 * * *") // 오전 4시마다 실행
    protected void updateMenuRanking() {
        log.info("[로그] updateRankings() 시작");

        // 어제 날짜 및 학기 구하기
        LocalDateTime now = LocalDateTime.now();
        LocalDate yesterday = now.toLocalDate().minusDays(1);
        int semester = getSemester(yesterday);

        // 어제 식단의 메뉴 ID 목록 조회
        List<Long> menuIdList = todayDietRepository.findMainMenuIdsByDate(yesterday);
        log.info("[로그] menuIdList = {}", menuIdList);

        // 메뉴 이름 및 식당 조회
        List<MenuInfoDto> menuInfoDtos = menuRepository.findMenusWithCafeteriaInMenuIds(menuIdList);
        log.info("[로그] menuInfoDtos = {}", menuInfoDtos);
        // 해당 학기 메뉴 Id의 메뉴 랭킹 조회
        Map<Long, MenuRanking> menuRankingMap = menuRankingRepository.findBySemesterInMenuIds(semester, menuIdList).stream()
                .collect(Collectors.toMap(MenuRanking::getMenuId, Function.identity()));
        log.info("[로그] menuRankingMap = {}", menuRankingMap);

        // 특정 메뉴 평균 점수 r (Ratings)
        // 특정 메뉴 리뷰 개수 c (count)
        Map<Long, MenuRatingStatsDto> menuRatingStatsDtoMap = reviewRepository.calculateRatingStatsByMainMenuIds(menuIdList).stream()
                .collect(Collectors.toMap(MenuRatingStatsDto::getMenuId, Function.identity()));
        log.info("[로그] menuRatingStatsDtoMap = {}", menuRatingStatsDtoMap);
        // 전체 메뉴 평균 점수 T (Total Ratings)
        Float totalAverageRating = reviewRepository.findAverageRating();
        log.info("[로그] totalAverageRating = {}", totalAverageRating);
        // 신뢰할 수 있는 리뷰 개수 C (Confidence Count)
        int confidenceCount = 10;
        // 리뷰 점수의 최대 값 m (Maximum Rating)
        float maximumRating = 5.0f;
        // 특정 메뉴 좋아요 수 l (Likes)
        Map<Long, MenuLikeCountDto> menuLikeCountMap = menuLikeRepository.countGroupByMenuIds(menuIdList).stream()
                .collect(Collectors.toMap(MenuLikeCountDto::getMenuId, Function.identity()));
        log.info("[로그] menuLikeCountMap = {}", menuLikeCountMap);

        // 좋아요 수의 가중치 w (Weight)
        float weight = 0.25f;   // 좋아요 4개 당 5점 리뷰 1개의 가치

        // 업데이트할 MenuRanking 리스트 생성
        List<MenuRanking> menuRankingsToUpdate = new ArrayList<>();

        // 베이지안 평균 (r*c + T*C) / (c + C)
        // 조정된 평점 = (r*c + T*C + m*l*w) / (c + C + l*w)
        menuInfoDtos.forEach(menuInfoDto -> {

            Long menuId = menuInfoDto.getMenuId();
            String menuName = menuInfoDto.getMenuName();
            String cafeteriaName = menuInfoDto.getCafeteriaName();
            String cafeteriaCorner = menuInfoDto.getCafeteriaCorner();

            MenuRatingStatsDto menuRatingStatsDto = menuRatingStatsDtoMap.get(menuId);
            if (menuRatingStatsDto == null) {
                log.warn("[로그] menuId가 {}인 MenuRatingStatsDto가 존재하지 않습니다", menuId);
                return; // continue
            }
            Long ratingSum = menuRatingStatsDto.getRatingSum();
            Long ratingCount = menuRatingStatsDto.getRatingCount();

            MenuLikeCountDto menuLikeCountDto = menuLikeCountMap.get(menuId);
            if (menuLikeCountDto == null) {
                log.warn("[로그] menuId가 {}인 MenuLikeCountDto가 존재하지 않습니다", menuId);
                return; // continue
            }
            Long likeCount = menuLikeCountDto.getMenuLikeCount();

            // 조정된 평점 계산
            float menuRating = (ratingSum + totalAverageRating * confidenceCount + maximumRating * likeCount * weight)
                                            / (ratingCount + confidenceCount + likeCount * weight);

            MenuRanking menuRanking = menuRankingMap.getOrDefault(menuId, new MenuRanking());
            menuRanking.updateMenuRanking(
                    menuId,
                    menuName,
                    menuRating,
                    cafeteriaName,
                    cafeteriaCorner,
                    semester,
                    ratingCount,
                    now
            );

            menuRankingsToUpdate.add(menuRanking);
        });

        // TODO: Bulk Insert로 변경
        menuRankingRepository.saveAll(menuRankingsToUpdate);
    }

    private static int getSemester(LocalDate localDate) {
        int todayYear = localDate.getYear();
        int todayMonth = localDate.getMonthValue();

        int semester = 0;
        if (todayMonth < 3) {
            semester = (todayYear - 1) * 100 + 9;
        } else if (todayMonth < 9) {
            semester = todayYear * 100 + 3;
        } else {
            semester = todayYear * 100 + 9;
        }
        return semester;
    }
}
