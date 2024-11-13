package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyReviewRes {

    @Schema(description = "리뷰 상세정보")
    private final Map<String, List<MyReviewDetailRes>> myReviewDetailList;
}
