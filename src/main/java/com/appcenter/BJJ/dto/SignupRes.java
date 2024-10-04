package com.appcenter.BJJ.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRes {
    String token;

    public SignupRes(String token) {
        this.token = token;
    }
}
