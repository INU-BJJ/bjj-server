package com.appcenter.BJJ.domain.review.dto;

import com.appcenter.BJJ.domain.review.domain.ReviewLike;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewLikeReq {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReviewLikePost {
        @Schema(description = "리뷰 id", example = "1")
        private Long reviewId;

        public ReviewLike toEntity(long memberId) {
            return ReviewLike.builder()
                    .memberId(memberId)
                    .reviewId(reviewId)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ReviewLikeDelete {
        @Schema(description = "리뷰 id", example = "1")
        private Long reviewId;
    }
}
