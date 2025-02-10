package com.appcenter.BJJ.domain.review.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BestReviewDto {
    private final Long menuId;
    private final Long bestReviewId;
}
