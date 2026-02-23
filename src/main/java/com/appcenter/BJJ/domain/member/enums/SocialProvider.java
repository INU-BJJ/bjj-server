package com.appcenter.BJJ.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {

    KAKAO("카카오"),
    NAVER("네이버"),
    GOOGLE("구글"),
    APPLE("애플"),
    BJJ("밥점줘"); // test용

    private final String description;
}
