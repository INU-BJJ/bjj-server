package com.appcenter.BJJ.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //400 Bad Request 서버 이해 못해
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "이메일 및 비밀번호가 맞지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 맞지 않습니다."),

    //401 Unauthorized 클라이언트 인증
    ERROR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자가 인증되지 않았습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "토큰의 형식이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 기간이 만료됐습니다."),

    //403 Forbidden 클라이언트는 아는데 접근 못해


    //404 Not Found 리소스를 찾을 수 없어
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원이 존재하지 않습니다."),

    //409 Conflict 중복됐어
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
    NICKNAME_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 닉네임입니다."),
    ERROR_SOCIAL_LOGIN_CONFLICT(HttpStatus.CONFLICT, "해당 소셜 계정은 이미 다른 계정에 연결되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus status, String msg) {
        httpStatus = status;
        message = msg;
    }
}
