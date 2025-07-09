package com.appcenter.BJJ.domain.item.dto;

import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class DetailItemRes extends ItemRes {

    private LocalDateTime expiresAt;

    private Boolean isWearing;

    private Boolean isOwned;

    public DetailItemRes(int itemIdx, String itemName, ItemType itemType, ItemLevel itemLevel,
                         LocalDateTime expiresAt, boolean isWearing, boolean isOwned) {
        super(itemIdx, itemName, itemType, itemLevel);
        this.expiresAt = expiresAt;
        this.isWearing = isWearing;
        this.isOwned = isOwned;
    }
}
