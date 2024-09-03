package com.appcenter.BJJ.dto;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TodayMenuRes {

    private final Long menuPairId;

    private final String mainMenuName;

    private final Long cafeteriaId;

    private final String cafeteriaName;

    private final String cafeteriaCorner;
}
