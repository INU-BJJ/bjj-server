package com.appcenter.BJJ.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TodayDietRes {

    private final Long id;

    private final String price;

    private final String kcal;

    private final LocalDate date;

    private final Long mainMenuId;

    private final String mainMenuName;

    private final Long subMenuId;

    private final String restMenu;

    private final String cafeteriaName;

    private final String cafeteriaCorner;

    //private Float starMean;

    //private String reviewImage;
}
