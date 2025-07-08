package com.appcenter.BJJ.domain.notification.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotifiableMemberDto {
    private final Long menuId;
    private final Long memberId;
}
