package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewImagesPagedRes {

    @Schema(description = "리뷰 이미지 상세정보")
    private final List<ReviewImageDetailRes> reviewImageDetailList;
    @Schema(description = "리뷰 이미지 마지막 페이지 여부", example = "true")
    private final boolean isLastPage;
}
