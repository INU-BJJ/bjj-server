package com.appcenter.BJJ.domain.todaydiet.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DietDto {
    private LocalDate date;
    private Long cafeteriaId;
    private String cafeteriaCorner;
    private Queue<String> menus = new ArrayDeque<>();
    private List<String> prices;
    private List<String> memberPrices;
    private List<String> calories;
    private String notification;

    @Builder
    private DietDto(LocalDate date, Long cafeteriaId, String cafeteriaCorner, String mainMenu, String price, String memberPrice, String calorie, String notification) {
        this.date = date;
        this.cafeteriaId = cafeteriaId;
        this.cafeteriaCorner = cafeteriaCorner;
        // 메인 메뉴만 받아서 입력
        this.menus = new ArrayDeque<>();
        menus.add(mainMenu);
        // 가격 하나만 받아서 입력
        this.prices = new ArrayList<>();
        prices.add(price);
        // 구성원 가격 하나만 받아서 입력
        this.memberPrices = new ArrayList<>();
        memberPrices.add(memberPrice);
        // 칼로리 하나만 받아서 입력
        this.calories = new ArrayList<>();
        calories.add(calorie);
        this.notification = notification;
    }

    public String pollMenu() {
        return menus.isEmpty() ? "" : menus.poll();
    }

    public String getPrice(int index) {
        return prices.isEmpty() ? "" : prices.get(Math.min(index, prices.size() - 1));
    }

    public String getMemberPrice(int index) {
        return memberPrices.isEmpty() ? "" : memberPrices.get(Math.min(index, memberPrices.size() - 1));
    }

    public String getCalorie(int index) {
        return calories.isEmpty() ? "" : calories.get(Math.min(index, calories.size() - 1));
    }

    public void updateMenus(Queue<String> newMenus) {
        this.menus = newMenus;
    }
}
