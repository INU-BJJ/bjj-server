package com.appcenter.BJJ.dto;

import com.appcenter.BJJ.domain.Image;
import com.appcenter.BJJ.domain.Review;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public interface ReviewReq {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class ReviewPost {

        private String comment;

        private Integer rating;

        private Long menuPairId;

        public Review toEntity(long memberId) {
            return Review.builder()
                    .comment(comment)
                    .rating(rating)
                    .memberId(memberId)
                    .menuPairId(menuPairId)
                    .build();
        }

    }
}
