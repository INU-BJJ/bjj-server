package com.appcenter.BJJ.domain.item.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyItemRes {

    private final String nickname;

    private final Long characterId;

    private final String characterImageName;

    private final Long backgroundId;

    private final String backgroundImageName;

    private final int point;
}