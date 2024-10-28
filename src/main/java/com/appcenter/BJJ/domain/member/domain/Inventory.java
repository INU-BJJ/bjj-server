package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory_tb")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long itemId;

    private boolean isWearing;

    private LocalDate validItem;
    @Builder
    private Inventory(Long memberId, Long itemId, boolean isWearing, LocalDate validItem) {
        this.memberId = memberId;
        this.itemId = itemId;
        this.isWearing = isWearing;
        this.validItem = validItem;
    }
}
