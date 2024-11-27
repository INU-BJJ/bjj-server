package com.appcenter.BJJ.domain.member.dto;

import com.appcenter.BJJ.domain.member.domain.Member;
import lombok.Getter;

import java.time.Instant;

@Getter
public class MemberVO {
    private final String provider;
    private final String providerId;
    private final String oauthToken;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public MemberVO(Member member) {
        this.provider = member.getProvider();
        this.providerId = member.getProviderId();
        this.oauthToken = member.getOauthToken();
        this.issuedAt = member.getIssuedAt();
        this.expiresAt = member.getExpiresAt();
    }
}
