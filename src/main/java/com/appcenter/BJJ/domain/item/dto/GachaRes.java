package com.appcenter.BJJ.domain.item.dto;

import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GachaRes {
    private final int itemId;
    private final String itemName;
    private final ItemType itemType;
    private final ItemLevel itemLevel;
    private final LocalDateTime validPeriod;
}
