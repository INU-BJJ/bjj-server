package com.appcenter.BJJ.domain.banner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BannerReq {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BannerPost {
        @NotBlank(message = "pageUri는 필수입니다.")
        @Schema(description = "배너 페이지 URI", example = "/view/reviews/best?period=DAY")
        private String pageUri;

        @Schema(description = "배너 노출 순서", example = "-1")
        private Integer sortOrder;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BannerPut {
        @Schema(description = "배너 페이지 URI", example = "/view/reviews/best?period=DAY")
        private String pageUri;

        @Schema(description = "배너 노출 순서", example = "-1")
        private Integer sortOrder;
    }
}
