package com.appcenter.BJJ.domain.item.dto;

import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemRes {
    private final int itemId;

    private final int price;

    private final ItemType itemType;

    private final ItemLevel itemLevel;

    private final String itemName;

//    private final boolean isWearing;
//
//    private final boolean isOwned;
//
//    private LocalDate validPeriod;
}
