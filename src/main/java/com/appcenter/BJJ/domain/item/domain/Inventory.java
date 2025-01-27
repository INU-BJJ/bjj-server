package com.appcenter.BJJ.domain.item.domain;

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

    private int itemId;

    private boolean isWearing;

    private LocalDate validPeriod;

    @Builder
    private Inventory(Long memberId, int itemId, boolean isWearing, LocalDate validPeriod) {
        this.memberId = memberId;
        this.itemId = itemId;
        this.isWearing = isWearing;
        this.validPeriod = validPeriod;
    }
}
