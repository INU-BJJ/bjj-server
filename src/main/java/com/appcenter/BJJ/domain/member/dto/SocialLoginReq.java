package com.appcenter.BJJ.domain.member.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SocialLoginReq {

    private final String provider;

    private final String providerId;
}
