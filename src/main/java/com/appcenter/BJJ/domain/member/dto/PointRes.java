package com.appcenter.BJJ.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointRes {
    //TODO test용이기에 이후에 없애기

    @Schema(description = "회원의 nickname", example = "이춘삼")
    private String nickname;

    @Schema(description = "회원의 point", example = "5000")
    private int point;
}
