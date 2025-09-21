package com.appcenter.BJJ.domain.banner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerRes {
    @Schema(description = "배너 이미지 이름", example = "092a97a2-19d9-4c67-b1a2-762e612425d0.png")
    private String imageName;
    @Schema(description = "배너 페이지 URI", example = "/view/reviews/best?period=DAY")
    private String pageUri;
}
