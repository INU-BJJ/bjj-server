package com.appcenter.BJJ.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginReq {
    @Schema(description = "회원의 nickname", example = "이춘삼")
    private String nickname;
    @Schema(description = "회원의 email", example = "asdf1234@gmail.com")
    private String email;
}
