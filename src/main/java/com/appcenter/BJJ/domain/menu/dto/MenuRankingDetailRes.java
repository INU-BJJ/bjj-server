package com.appcenter.BJJ.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class MenuRankingDetailRes {

    @Schema(description = "메뉴 id", example = "1")
    private final Long menuId;
    @Schema(description = "메뉴 이름", example = "등심돈까스")
    private final String menuName;
    @Schema(description = "메뉴 평점", example = "4.5")
    private final Float menuRating;
    @Schema(description = "식당 이름", example = "학생식당")
    private final String cafeteriaName;
    @Schema(description = "식당 코너 이름", example = "중식(백반)")
    private final String cafeteriaCorner;
    @Schema(description = "베스트 리뷰 id", example = "1")
    private final Long bestReviewId;
    @Schema(description = "리뷰 사진 파일 이름", example = "23fsddfesff=3vlsdd-3sdf56.png")
    private String reviewImageName;
    @Schema(description = "랭킹 최근 업데이트 날짜", example = "2025-01-06", deprecated = true)
    private final LocalDate updatedAt;

    @Builder
    private MenuRankingDetailRes(Long menuId, String menuName, Float menuRating, String cafeteriaName, String cafeteriaCorner, Long bestReviewId, LocalDateTime updatedAt) {
        this.menuId = menuId;
        this.menuName = menuName;
        // 클라이언트 요구사항에 따라 10점 만점으로 변경 및 소수점 첫째자리로 제한
        this.menuRating = Math.round(menuRating * 2 * 10) / 10.0f;
        this.cafeteriaName = cafeteriaName;
        this.cafeteriaCorner = cafeteriaCorner;
        this.bestReviewId = bestReviewId;
        // 클라이언트 요구사항에 따라 업데이트 날짜를 하루 전으로 표기 및 연-월-일만 표기하도록 변환
        this.updatedAt = updatedAt.minusDays(1).toLocalDate();
    }

    public void initReviewImageName(String reviewImageName) {
        this.reviewImageName = reviewImageName;
    }
}
