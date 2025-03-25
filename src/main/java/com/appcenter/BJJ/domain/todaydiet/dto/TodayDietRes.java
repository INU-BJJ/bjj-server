package com.appcenter.BJJ.domain.todaydiet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TodayDietRes {
    @Schema(description = "오늘 식단 id", example = "1")
    private final Long todayDietId;
    @Schema(description = "식단 가격", example = "6,000원")
    private final String price;
    @Schema(description = "식단 칼로리", example = "1531kcal")
    private final String kcal;
    @Schema(description = "식단이 나온 날짜", example = "2024-09-23")
    private final LocalDate date;
    @Schema(description = "메뉴쌍 id", example = "1")
    private final Long menuPairId;
    @Schema(description = "메인메뉴 id", example = "1")
    private final Long mainMenuId;
    @Schema(description = "메인메뉴 이름", example = "등심돈까스")
    private final String mainMenuName;
    @Schema(description = "서브메뉴 id", example = "2")
    private final Long subMenuId;
    @Schema(description = "서브메뉴 이름", example = "우동국물 새우까스*칠리s 쫄면 단무지")
    private final String restMenu;
    @Schema(description = "식당 이름", example = "학생식당")
    private final String cafeteriaName;
    @Schema(description = "식당 코너 이름", example = "중식(백반)")
    private final String cafeteriaCorner;
    @Schema(description = "리뷰 총 개수", example = "35")
    private final int reviewCount;
    @Schema(description = "메뉴 평균 평점", example = "4.7")
    private final Float reviewRatingAverage;
    @Schema(description = "리뷰 사진 파일 이름", example = "23fsddfesff=3vlsdd-3sdf56.png")
    @Setter
    private String reviewImageName;
    @Schema(description = "좋아하는 메뉴 여부", example = "true")
    private final boolean isLikedMenu;
}
