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

    private Integer itemId;

    private String itemName;

    private String imageName;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    private ItemLevel itemLevel;

    @Builder
    private Item(Integer itemId, String itemName, String imageName, ItemType itemType, ItemLevel itemLevel) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.imageName = imageName;
        this.itemType = itemType;
        this.itemLevel = itemLevel;
    }

    public static Item create(ItemVO itemVO, String imageName, ItemType itemType) {
        return Item.builder()
                .itemId(itemVO.getItemId())
                .itemName(itemVO.getItemName())
                .imageName(imageName)
                .itemType(itemType)
                .itemLevel(itemVO.getItemLevel())
                .build();
    }
}
