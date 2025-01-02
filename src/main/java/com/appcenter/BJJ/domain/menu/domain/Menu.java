package com.appcenter.BJJ.domain.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuName;

    private Integer likeCount;

    private Long cafeteriaId;

    @Builder
    private Menu(String menuName, Long cafeteriaId) {
        this.menuName = menuName;
        this.cafeteriaId = cafeteriaId;
        this.likeCount = 0;
    }

    public void incrementLikeCount() {
        ++likeCount;
    }

    public void decrementLikeCount() {
        if (likeCount > 0) {
            --likeCount;
        }
    }
}
