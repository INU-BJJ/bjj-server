package com.appcenter.BJJ.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberRes {
    @Schema(description = "회원의 nickname", example = "이춘삼")
    private String nickname;
    @Schema(description = "회원의 email", example = "asdf1234@gmail.com")
    private String email;
    @Schema(description = "소셜로그인 제공자", example = "google")
    private String provider;
}