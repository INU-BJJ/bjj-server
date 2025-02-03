package com.appcenter.BJJ.domain.menu.dto;

import lombok.*;

@Getter
@Builder
@ToString
public class MenuRatingStatsDto {
    private final Long menuId;
    private final Long ratingSum;
    private final Long ratingCount;
}
