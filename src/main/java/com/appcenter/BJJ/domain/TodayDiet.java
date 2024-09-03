package com.appcenter.BJJ.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "today_diet_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodayDiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String price;

    private String kcal;

    private LocalDate date;

    private Long mainMenuId;

    private Long subMenuId;

    private String restMenu;

    @Builder
    private TodayDiet(String price, String kcal, LocalDate date) {
        this.price = price;
        this.kcal = kcal;
        this.date = date;
    }

    public void determineMenu(Long mainMenuId) {
        this.mainMenuId = mainMenuId;
        this.subMenuId = null;
        this.restMenu = null;
    }

    public void determineMenu(Long mainMenuId, Long subMenuId, String restMenu) {
        this.mainMenuId = mainMenuId;
        this.subMenuId = subMenuId;
        this.restMenu = restMenu;
    }
}
