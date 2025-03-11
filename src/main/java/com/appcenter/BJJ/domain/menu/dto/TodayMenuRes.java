package com.appcenter.BJJ.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TodayMenuRes {
    @Schema(description = "메뉴쌍 id", example = "1")
    private final Long menuPairId;
    @Schema(description = "메인메뉴 이름", example = "4.7")
    private final String mainMenuName;
    @Schema(description = "식당 id", example = "1")
    private final Long cafeteriaId;
    @Schema(description = "식당 이름", example = "학생식당")
    private final String cafeteriaName;
    @Schema(description = "식당 코너 이름", example = "중식(백반)")
    private final String cafeteriaCorner;
}
