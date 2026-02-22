package com.appcenter.BJJ.domain.member.dto;

import com.appcenter.BJJ.domain.member.enums.SocialProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberRes {
    @Schema(description = "회원의 nickname", example = "이춘삼")
    private final String nickname;
    @Schema(description = "회원의 email", example = "asdf1234@gmail.com")
    private final String email;
    @Schema(description = "소셜로그인 제공자", example = "google")
    private final SocialProvider provider;
}
