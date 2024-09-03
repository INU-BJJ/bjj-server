package com.appcenter.BJJ.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TodayDietRes {

    private Long id;

    private String price;

    private String kcal;

    private LocalDate date;

    private Long mainMenuId;

    private String mainMenuName;

    private Long subMenuId;

    private String restMenu;

    private String cafeteriaName;

    private String cafeteriaCorner;

    //private Float starMean;

    //private String reviewImage;
}
