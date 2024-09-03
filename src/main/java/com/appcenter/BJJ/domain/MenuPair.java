package com.appcenter.BJJ.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu_pair_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mainMenuId;

    private Long subMenuId;

    private Float reviewAverageRating;

    @Builder
    private MenuPair(Long mainMenuId, Long subMenuId) {
        this.mainMenuId = mainMenuId;
        this.subMenuId = subMenuId;
        this.reviewAverageRating = 0F;
    }
}
