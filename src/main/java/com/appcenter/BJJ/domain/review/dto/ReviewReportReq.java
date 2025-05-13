package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ReviewReportReq {

    @Schema(description = "리뷰 신고 내용", example = "사진을 못 찍으셔요.")
    private String content;
}
