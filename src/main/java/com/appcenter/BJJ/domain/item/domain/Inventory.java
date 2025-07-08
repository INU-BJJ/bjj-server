package com.appcenter.BJJ.domain.item.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventory_tb")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private int itemIdx;

    private Boolean isWearing;

    private Boolean isOwned;

    private LocalDateTime validPeriod;

    @Builder
    private Inventory(Long memberId, int itemIdx, boolean isWearing, boolean isOwned, LocalDateTime validPeriod) {
        this.memberId = memberId;
        this.itemIdx = itemIdx;
        this.isOwned = isOwned;
        this.isWearing = isWearing;
        this.validPeriod = validPeriod;
    }

    public void updateValidPeriodAndIsOwned(LocalDateTime validPeriod) {
        this.validPeriod = validPeriod.plusDays(7);
        this.isOwned = true;
    }

    public void toggleIsWearing() {
        this.isWearing = !isWearing;
    }
}
