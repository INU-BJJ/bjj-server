package com.appcenter.BJJ.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor // QueryDSL은 접근제어자로 Public 필요
public class BestReviewDto {
    private final Long menuId;
    private final Long bestReviewId;
}
