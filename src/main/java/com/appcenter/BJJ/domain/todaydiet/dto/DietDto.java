package com.appcenter.BJJ.domain.todaydiet.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DietDto {
    private LocalDate date;
    private Long cafeteriaId;
    private String cafeteriaCorner;
    private Queue<String> menus;
    private String price;
    private String memberPrice;
    private List<String> calories;
    private String notification;

    @Builder
    private DietDto(LocalDate date, Long cafeteriaId, String cafeteriaCorner, String mainMenu, String price, String memberPrice, String calorie, String notification) {
        this.date = date;
        this.cafeteriaId = cafeteriaId;
        this.cafeteriaCorner = cafeteriaCorner;
        // 메인 메뉴만 받아서 입력
        this.menus = new LinkedList<>();
        menus.add(mainMenu);
        this.price = price;
        this.memberPrice = memberPrice;
        // 칼로리 하나만 받아서 입력
        this.calories = new ArrayList<>();
        calories.add(calorie);
        this.notification = notification;
    }

    public String getCalorie() {
        return calories.isEmpty() ? "" : calories.get(0);
    }
}
