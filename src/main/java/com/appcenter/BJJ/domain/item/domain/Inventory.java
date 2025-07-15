package com.appcenter.BJJ.domain.item.domain;

import com.appcenter.BJJ.domain.item.enums.ItemType;
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

    private Integer itemIdx;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    private Boolean isWearing;

    private Boolean isOwned;

    private LocalDateTime expiresAt;

    @Builder
    private Inventory(Long memberId, int itemIdx, ItemType itemType, boolean isWearing, boolean isOwned, LocalDateTime expiresAt) {
        this.memberId = memberId;
        this.itemIdx = itemIdx;
        this.itemType = itemType;
        this.isOwned = isOwned;
        this.isWearing = isWearing;
        this.expiresAt = expiresAt;
    }

    public static Inventory createDefault(Long memberId, ItemType itemType){
        return Inventory.builder()
                .memberId(memberId)
                .itemIdx(0)
                .itemType(itemType)
                .isOwned(true)
                .isWearing(true)
                .expiresAt(LocalDateTime.of(9999, 12, 31, 23, 59, 59))
                .build();
    }

    public void updateValidPeriodAndIsOwned(LocalDateTime validPeriod) {
//        this.expiresAt = validPeriod.plusDays(7); TODO 프론트 테스트를 위해 잠시 15초로 변경
        this.expiresAt = validPeriod.plusSeconds(20);
        this.isOwned = true;
    }

    public void toggleIsWearing() {
        this.isWearing = !isWearing;
    }
}
