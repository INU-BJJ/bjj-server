package com.appcenter.BJJ.domain.member.domain;

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
    Long id;

    String nickname;

    String email;

    String provider;

    String providerId;

    int point;

    String role;

    @Builder
    private Member(String nickname, String email, String provider, String providerId) {
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.point = 0;
        this.role = "ROLE_GUEST";
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
}
