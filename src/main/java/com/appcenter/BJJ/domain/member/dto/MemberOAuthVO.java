package com.appcenter.BJJ.domain.member.dto;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.domain.OAuth2Client;
import lombok.Getter;

import java.time.Instant;

@Getter
public class MemberOAuthVO {
    private final Long memberId;
    private final String provider;
    private final String providerId;
    private final String oauthToken;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public MemberOAuthVO(Member member) {
        this.memberId = member.getId();
        this.provider = member.getProvider();
        this.providerId = member.getProviderId();
        this.oauthToken = member.getOAuth2Client().getOauthToken();
        this.issuedAt = member.getOAuth2Client().getIssuedAt();
        this.expiresAt = member.getOAuth2Client().getExpiresAt();
    }
}
