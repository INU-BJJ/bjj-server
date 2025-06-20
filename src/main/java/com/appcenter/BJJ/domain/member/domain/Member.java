package com.appcenter.BJJ.domain.member.domain;

import com.appcenter.BJJ.domain.member.enums.MemberRole;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Enumerated(value = EnumType.STRING)
    private MemberStatus memberStatus;

    @Embedded
    private SuspensionPeriod suspensionPeriod;

    @Embedded
    private OAuth2Client oAuth2Client;

    private Boolean isNotificationEnabled;


    @Builder
    private Member(String nickname, String email, String provider, String providerId, OAuth2Client oAuth2Client) {
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.point = 0;
        this.role = MemberRole.GUEST;
        this.memberStatus = MemberStatus.ACTIVE;
        this.suspensionPeriod = SuspensionPeriod.init();
        this.oAuth2Client = oAuth2Client;
        this.isNotificationEnabled = true;
    }

    public void updateMemberInfo(String nickname, MemberRole role) {
        this.nickname = nickname;
        this.role = role;
    }

    public void decreasePoint(int point) {
        this.point -= point;
    }

    public void increasePoint(int point) {
        this.point += point;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateOauthToken(OAuth2Client oAuth2Client) {
        this.oAuth2Client = oAuth2Client;
    }

    public void toggleNotification() {
        this.isNotificationEnabled = !this.isNotificationEnabled;
    }

    // test용 메소드
    public void updateMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    public void suspend(LocalDateTime startAt, LocalDateTime endAt) {
        this.suspensionPeriod.suspend(startAt, endAt);
    }

    //TODO test용이기에 이후에 없애기
    public void updateTestProviderId(String id) {
        this.providerId = id;
    }
}
