package com.appcenter.BJJ.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewDetailRes {

    private final Long reviewId;

    private final String comment;

    private final Integer rating;

    private final List<String> imagePaths;

    private final Long likeCount;

    private final boolean isLikedMenu;

    private final LocalDate createdDate;

    private final Long menuPairId;

    private final Long mainMenuId;

    private final Long subMenuId;

    private final Long memberId;

    private final String memberNickname;

    private final String memberImagePath;
}
