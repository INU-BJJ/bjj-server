package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuPairService {

    private final MenuPairRepository menuPairRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void refreshReviewCountAndRating(Long menuPairId) {
        log.info("[로그] refreshReviewCountAndRating() 시작");

        int reviewCount = reviewRepository.countByMenuPair_Id(menuPairId);
        Float reviewAverageRating = reviewRepository.calculateAverageRatingByMenuPairId(menuPairId);

        MenuPair menuPair = menuPairRepository.findById(menuPairId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_PAIR_NOT_FOUND));
        log.info("[로그] reviewCount : {}, reviewAverageRating : {}, menuPair.getId() : {}", reviewCount, reviewAverageRating, menuPair.getId());

        menuPair.updateReviewCount(reviewCount);
        menuPair.updateReviewAverageRating(reviewAverageRating);

        menuPairRepository.save(menuPair);
    }

    @Transactional
    public Long getOrCreateMenuPair(Long mainMenuId, Long subMenuId) {
        return menuPairRepository.findFirstByMainMenuIdAndSubMenuId(mainMenuId, subMenuId)
                .orElseGet(() -> menuPairRepository.save(MenuPair.builder()
                        .mainMenuId(mainMenuId)
                        .subMenuId(subMenuId)
                        .build())
                ).getId();
    }
}
