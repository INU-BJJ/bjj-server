package com.appcenter.BJJ.domain.member.domain;

import com.appcenter.BJJ.domain.member.enums.MemberRole;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.member.enums.SocialProvider;
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

    @Enumerated(value = EnumType.STRING)
    private SocialProvider provider;

    private String providerId;

    private int point;

    @Enumerated(value = EnumType.STRING)
    private MemberRole role;

    @Enumerated(value = EnumType.STRING)
    private MemberStatus memberStatus;

    private Boolean isNotificationEnabled;


    @Builder
    private Member(String nickname, String email, SocialProvider provider, String providerId) {
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.point = 0;
        this.role = MemberRole.USER;
        this.memberStatus = MemberStatus.ACTIVE;
        this.isNotificationEnabled = true;
    }

    public static Member create(String nickname, String email, SocialProvider provider, String providerId) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .build();
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

    public void toggleNotification() {
        this.isNotificationEnabled = !this.isNotificationEnabled;
    }

    //TODO test용이기에 이후에 없애기
    public void updateMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    //TODO test용이기에 이후에 없애기
    public void updateTestProviderId(String id) {
        this.providerId = id;
    }
}
