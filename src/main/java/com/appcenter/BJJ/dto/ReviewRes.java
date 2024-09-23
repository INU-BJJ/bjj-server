package com.appcenter.BJJ.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewRes {

    private final Long id;

    private final String comment;

    private final Integer rating;

    private final List<String> imagePaths;

    private final Long likeCount;

    private final LocalDate createdDate;

    private final Long memberId;

    private final Long menuPairId;

    private final Long mainMenuId;

    private final Long subMenuId;
}
