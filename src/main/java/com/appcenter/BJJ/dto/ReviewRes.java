package com.appcenter.BJJ.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewRes {
    @Schema(description = "리뷰 상세정보")
    private final List<ReviewDetailRes> reviewDetailList;
    @Schema(description = "리뷰 마지막 페이지 여부", example = "true")
    private final boolean isLastPage;
}
