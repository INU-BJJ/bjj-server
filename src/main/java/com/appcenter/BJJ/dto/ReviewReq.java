package com.appcenter.BJJ.dto;

import com.appcenter.BJJ.domain.MenuPair;
import com.appcenter.BJJ.domain.Review;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public interface ReviewReq {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class ReviewPost {

        private String comment;

        private Integer rating;

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
