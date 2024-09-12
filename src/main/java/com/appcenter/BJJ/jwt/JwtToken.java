package com.appcenter.BJJ.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtToken {
    private String grantType;
    private String accessToken;
}
