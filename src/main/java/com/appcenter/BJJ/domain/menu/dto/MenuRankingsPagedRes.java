package com.appcenter.BJJ.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter

public class MenuRankingsPagedRes {
    @Schema(description = "메뉴 랭킹 상세정보")
    private final List<MenuRankingDetailRes> menuRankingDetailList;
    @Schema(description = "메뉴 랭킹 마지막 페이지 여부", example = "true")
    private final boolean isLastPage;
    @Schema(description = "메뉴 랭킹 업데이트 날짜", example = "2025-08-02")
    private final LocalDate updatedAt;

    @Builder
    public MenuRankingsPagedRes(List<MenuRankingDetailRes> menuRankingDetailList, boolean isLastPage, LocalDateTime updatedAt) {
        this.menuRankingDetailList = menuRankingDetailList;
        this.isLastPage = isLastPage;
        // 클라이언트 요구사항에 따라 업데이트 날짜를 하루 전으로 표기 및 연-월-일만 표기하도록 변환
        this.updatedAt = updatedAt.minusDays(1).toLocalDate();
    }
}
