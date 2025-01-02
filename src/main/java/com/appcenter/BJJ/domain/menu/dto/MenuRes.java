package com.appcenter.BJJ.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuRes {
    @Schema(description = "메뉴 id", example = "1")
    private final Long menuId;
    @Schema(description = "메뉴 이름", example = "우삼겹떡볶이*핫도그")
    private final String menuName;
}
