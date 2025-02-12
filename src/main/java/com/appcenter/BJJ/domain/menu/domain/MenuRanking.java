package com.appcenter.BJJ.domain.menu.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@ToString
@Table(name = "menu_ranking_tb")
@NoArgsConstructor
public class MenuRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long menuId;
    private String menuName;
    private Float menuRating;
    private Long ratingCount;
    private String cafeteriaName;
    private String cafeteriaCorner;
    private Integer semester;
    private LocalDateTime updatedAt;

    public void updateMenuRanking(Long menuId, String menuName, Float menuRating, String cafeteriaName, String cafeteriaCorner, Integer semester, Long ratingCount, LocalDateTime updatedAt) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuRating = menuRating;
        this.cafeteriaName = cafeteriaName;
        this.cafeteriaCorner = cafeteriaCorner;
        this.semester = semester;
        this.ratingCount = ratingCount;
        this.updatedAt = updatedAt;
    }
}
