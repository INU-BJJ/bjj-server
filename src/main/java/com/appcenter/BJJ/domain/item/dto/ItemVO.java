package com.appcenter.BJJ.domain.item.dto;

import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ItemVO {
    private final Integer itemId;

    private final int price;

    private final ItemLevel itemLevel;

    @Setter
    private ItemType itemType;

    @Setter
    private String imageName;
}
