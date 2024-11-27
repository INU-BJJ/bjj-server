package com.appcenter.BJJ.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginReq {
    @Schema(description = "회원의 nickname", example = "이춘삼")
    @NotBlank(message = "닉네임은 필수항목입니다.")
    private final String nickname;
    @Schema(description = "회원의 email", example = "asdf1234@gmail.com")
    @NotBlank(message = "이메일은 필수항목입니다.")
    private final String email;
}
