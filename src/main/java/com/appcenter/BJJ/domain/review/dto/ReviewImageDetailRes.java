package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewImageDetailRes {

    @Schema(description = "리뷰 id", example = "1")
    private final Long reviewId;
    @Schema(description = "리뷰 이미지 이름", example = "23fsddfesff=3vlsdd-3sdf56.png")
    private final String imageName;
}
