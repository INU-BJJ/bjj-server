package com.appcenter.BJJ.domain.review.dto;

import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewReq {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReviewPost {
        @Schema(description = "리뷰 내용", example = "맛이 차암 좋읍니다.")
        private String comment;
        @Schema(description = "리뷰 별점", example = "5")
        private Integer rating;
        @Schema(description = "리뷰 메뉴쌍 id", example = "1")
        private Long menuPairId;

        public Review toEntity(long memberId, MenuPair menuPair) {
            return Review.builder()
                    .comment(comment)
                    .rating(rating)
                    .memberId(memberId)
                    .menuPair(menuPair)
                    .build();
        }
    }
}
