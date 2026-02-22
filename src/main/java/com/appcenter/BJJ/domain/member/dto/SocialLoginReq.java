package com.appcenter.BJJ.domain.member.dto;

import com.appcenter.BJJ.domain.member.enums.SocialProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SocialLoginReq {

    private final SocialProvider provider;

    private final String providerId;
}
