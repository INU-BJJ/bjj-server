package com.appcenter.BJJ.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponseDTO {
    String accessToken;
    String refreshToken;
}
