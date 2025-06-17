package com.appcenter.BJJ.domain.notification.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberDeviceTokenDto {
    private final Long memberId;
    private final String token;
}
