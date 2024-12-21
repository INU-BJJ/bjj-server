package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2Client {
    private String oauthToken;
    private Instant issuedAt;
    private Instant expiresAt;
}
