package com.appcenter.BJJ.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MemberRes {
    private String nickname;
    private String email;
    private String provider;
}
