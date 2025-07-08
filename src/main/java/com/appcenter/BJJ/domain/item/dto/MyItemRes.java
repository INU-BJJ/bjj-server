package com.appcenter.BJJ.domain.item.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyItemRes {

    private final String nickname;

    private final Integer characterIdx; //Idx값의 null값을 허용

    private final String characterImageName;

    private final Integer backgroundIdx;

    private final String backgroundImageName;

    private final int point;
}