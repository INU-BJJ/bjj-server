package com.appcenter.BJJ.domain.menu.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuLikeCountDto {
    private final Long menuId;
    private final Long menuLikeCount;
}
