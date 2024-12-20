package com.appcenter.BJJ.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupReq {
    @Schema(description = "회원 닉네임", example = "이춘삼")
    @Size(max = 12, message = "닉네임은 12글자까지 가능합니다.")
    @NotBlank(message = "닉네임은 필수항목입니다.")
    private final String nickname;

    @Email
    @Schema(description = "회원 이메일", example = "asdf@gmail.com")
    @NotBlank(message = "이메일은 필수항목입니다.")
    private final String email;

    @Schema(description = "소셜로그인 제공자", example = "google")
    @NotBlank(message = "provider와 함께 요청해주세요")
    private final String provider;
}
