package com.appcenter.BJJ.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SignupDTO {
    @NotBlank(message = "닉네임은 필수항목입니다.")
    @Size(max = 12, message = "닉네임은 12글자까지 가능합니다.")
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String provider;
}
