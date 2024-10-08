package com.appcenter.BJJ.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SignupReq {
    @NotBlank(message = "닉네임은 필수항목입니다.")
    @Size(max = 12, message = "닉네임은 12글자까지 가능합니다.")
    @Schema(description = "회원 닉네임", example = "이춘삼")
    private String nickname;

    @Email
    @NotBlank
    @Schema(description = "회원 이메일", example = "asdf@gmail.com")
    private String email;

    @NotBlank
    @Schema(description = "소셜로그인 제공자", example = "google")
    private String provider;
}
