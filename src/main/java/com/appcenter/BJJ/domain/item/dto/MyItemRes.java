package com.appcenter.BJJ.domain.item.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MyItemRes extends ItemRes {

    private ItemVO itemVO;

    private String nickname;

    private int point;
}