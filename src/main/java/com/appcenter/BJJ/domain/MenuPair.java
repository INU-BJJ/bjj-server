package com.appcenter.BJJ.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Entity
@Table(name = "menu_pair_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mainMenuId;

    private Long subMenuId;

    private Float reviewAverageRating;

    @Builder
    private MenuPair(Long mainMenuId, Long subMenuId) {
        this.mainMenuId = mainMenuId;
        this.subMenuId = subMenuId;
        this.reviewAverageRating = 0F;
    }

    public void updateReviewAverageRating(Float reviewAverageRating) {
        // 소수점 이하 2자리로 제한
        BigDecimal roundedAverage = BigDecimal.valueOf(reviewAverageRating)
                .setScale(2, RoundingMode.HALF_UP);

        this.reviewAverageRating = roundedAverage.floatValue();
    }
}
