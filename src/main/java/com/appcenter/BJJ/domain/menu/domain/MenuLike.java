package com.appcenter.BJJ.domain.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menu_like_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long menuId;

    @Builder
    private MenuLike(Long memberId, Long menuId) {
        this.memberId = memberId;
        this.menuId = menuId;
    }
}
