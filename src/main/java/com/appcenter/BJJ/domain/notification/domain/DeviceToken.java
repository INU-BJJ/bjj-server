package com.appcenter.BJJ.domain.notification.domain;


import com.appcenter.BJJ.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "device_token_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

    private Boolean isActive;

    @Builder
    private DeviceToken(Member member, String token) {
        this.member = member;
        this.token = token;
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateLastUsedAt(LocalDateTime dateTime) {
        this.lastUsedAt = dateTime;
    }
}
