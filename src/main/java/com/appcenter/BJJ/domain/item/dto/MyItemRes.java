package com.appcenter.BJJ.domain.item.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyItemRes {

    private final String nickname;

    private final Integer itemId;

    private final String imageName;

    private final int point;
}