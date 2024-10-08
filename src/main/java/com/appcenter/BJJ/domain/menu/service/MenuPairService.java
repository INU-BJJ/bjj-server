package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuPairService {

    private final MenuPairRepository menuPairRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void refreshReviewCountAndRating(Long menuPairId) {

        int reviewCount = reviewRepository.countByMenuPair_Id(menuPairId);
        Float reviewAverageRating = reviewRepository.findAverageRatingByMenuPairId(menuPairId);

        MenuPair menuPair = menuPairRepository.findById(menuPairId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid menuPair ID"));

        menuPair.updateReviewCount(reviewCount);
        menuPair.updateReviewAverageRating(reviewAverageRating);

        menuPairRepository.save(menuPair);
    }
}
