package com.appcenter.BJJ.domain.item.domain;

import com.appcenter.BJJ.domain.item.dto.ItemVO;
import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "item_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    private ItemLevel itemLevel;

    @Builder
    private Item(String itemName, ItemType itemType, ItemLevel itemLevel) {
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemLevel = itemLevel;
    }

    public static Item create(ItemVO itemVO, ItemType itemType) {
        return Item.builder()
                .itemName(itemVO.getItemName())
                .itemType(itemType)
                .itemLevel(itemVO.getItemLevel())
                .build();
    }
}
