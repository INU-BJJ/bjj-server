package com.appcenter.BJJ.domain.member.dto;

import com.appcenter.BJJ.domain.member.domain.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberOAuthVO {
    private Long memberId;
    private String provider;
    private String providerId;
    private String oauthToken;
    private Instant issuedAt;
    private Instant expiresAt;

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
