package com.appcenter.BJJ.domain.member.domain;

import com.appcenter.BJJ.domain.member.MemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Enumerated(value = EnumType.STRING)
    private MemberRole role;

    private OAuth2Client oAuth2Client;


    @Builder
    private Member(String nickname, String email, String provider, String providerId, OAuth2Client oAuth2Client) {
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.point = 0;
        this.role = MemberRole.GUEST;
        this.oAuth2Client = oAuth2Client;
    }

    public void updateMemberInfo(String nickname, MemberRole role) {
        this.nickname = nickname;
        this.role = role;
    }

    public void updatePoint(int point) {
        this.point -= point;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateOauthToken(OAuth2Client oAuth2Client) {
        this.oAuth2Client = oAuth2Client;
    }

    // test용 메소드
    public void updateTestProviderId(String id) {
        this.providerId = id;
    }
}
