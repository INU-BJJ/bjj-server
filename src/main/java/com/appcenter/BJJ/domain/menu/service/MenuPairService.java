package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("Invalid menuPair ID"));
        log.info("[로그] reviewCount : {}, reviewAverageRating : {}, menuPair.getId() : {}", reviewCount, reviewAverageRating, menuPair.getId());

        menuPair.updateReviewCount(reviewCount);
        menuPair.updateReviewAverageRating(reviewAverageRating);

        menuPairRepository.save(menuPair);
    }
}
