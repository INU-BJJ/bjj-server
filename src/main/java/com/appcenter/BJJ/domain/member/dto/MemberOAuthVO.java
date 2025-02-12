package com.appcenter.BJJ.domain.member.dto;

import com.appcenter.BJJ.domain.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class MemberOAuthVO {
    private final Long memberId;
    private final String provider;
    private final String providerId;
    private final String oauthToken;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public static MemberOAuthVO from(Member member) {
        return MemberOAuthVO.builder()
                .memberId(member.getId())
                .provider(member.getProvider())
                .providerId(member.getProviderId())
                .oauthToken(member.getOAuth2Client().getOauthToken())
                .issuedAt(member.getOAuth2Client().getIssuedAt())
                .expiresAt(member.getOAuth2Client().getExpiresAt())
                .build();
    }
}
