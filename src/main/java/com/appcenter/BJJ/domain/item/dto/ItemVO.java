package com.appcenter.BJJ.domain.item.dto;

import com.appcenter.BJJ.domain.item.enums.ItemLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemVO {

    private final Integer itemIdx;

    private final String itemName;

    private final ItemLevel itemLevel;
}
