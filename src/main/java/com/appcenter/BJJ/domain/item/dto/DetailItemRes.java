package com.appcenter.BJJ.domain.item.dto;

import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class DetailItemRes extends ItemRes {

    private LocalDateTime validPeriod;

    private Boolean isWearing;

    private Boolean isOwned;

    public DetailItemRes(Integer itemId, String itemName, ItemType itemType, ItemLevel itemLevel, String imageName,
                         LocalDateTime validPeriod, boolean isWearing, boolean isOwned) {
        super(itemId, itemName, itemType, itemLevel, itemName);
        this.validPeriod = validPeriod;
        this.isWearing = isWearing;
        this.isOwned = isOwned;
    }
}
