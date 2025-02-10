package com.appcenter.BJJ.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuRankingPagedRes {
    @Schema(description = "메뉴 랭킹 상세정보")
    private final List<MenuRankingDetailRes> menuRankingDetailList;
    @Schema(description = "메뉴 랭킹 마지막 페이지 여부", example = "true")
    private final boolean isLastPage;
}
