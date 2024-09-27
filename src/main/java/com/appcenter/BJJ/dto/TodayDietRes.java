package com.appcenter.BJJ.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TodayDietRes {

    private final Long id;

    private final String price;

    private final String kcal;

    private final LocalDate date;

    private final Long menuPairId;

    private final Long mainMenuId;

    private final String mainMenuName;

    private final Long subMenuId;

    private final String restMenu;

    private final String cafeteriaName;

    private final String cafeteriaCorner;

    private final int reviewCount;

    private final Float reviewRatingAverage;

    @Setter
    private String reviewImagePath;

    @Setter
    private boolean isLikedMenu;
}
