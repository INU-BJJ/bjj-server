package com.appcenter.BJJ.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nickname;

    String email;

    String provider;

    String providerId;

    @Builder.Default
    Long point = 0L;

    @Builder.Default
    String role = "ROLE_GUEST";

    public void update(String role) {
        this.role = role;
    }
}
