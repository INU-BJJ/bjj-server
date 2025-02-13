package com.appcenter.BJJ.domain.item.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MyItemRes extends ItemRes {

    @JsonUnwrapped
    private ItemVO itemVO;

    private String nickname;

    private int point;
}