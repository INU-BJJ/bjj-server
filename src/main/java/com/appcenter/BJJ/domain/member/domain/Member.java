package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_tb")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String email;

    private String provider;

    private String providerId;

    private int point;

    private String role;

    private String oauthToken;

    private Instant issuedAt;

    private Instant expiresAt;


    @Builder
    private Member(String nickname, String email, String provider, String providerId, String oauthToken, Instant issuedAt, Instant expiresAt) {
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.point = 0;
        this.role = "ROLE_GUEST";
        this.oauthToken = oauthToken;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public void updateMemberInfo(String nickname, String role) {
        this.nickname = nickname;
        this.role = role;
    }

    public void updatePoint(int point) {
        this.point += point;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateOauthToken(String oauthToken, Instant issuedAt, Instant expiresAt) {
        this.oauthToken = oauthToken;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    // test용 메소드
    public void updateTestProviderId(String id) {
        this.providerId = id;
    }
}
