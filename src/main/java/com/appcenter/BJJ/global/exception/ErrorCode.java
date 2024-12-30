package com.appcenter.BJJ.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //400 Bad Request
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "이메일 및 비밀번호가 맞지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 맞지 않습니다."),

    //401 Unauthorized
    ERROR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "토큰의 형식이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 기간이 만료됐습니다."),

    //403 Forbidden
    ERROR_FORBIDDEN(HttpStatus.FORBIDDEN, "이 리소스에 접근할 권한이 없습니다."),

    //404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원이 존재하지 않습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 아이템이 존재하지 않습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 메뉴가 존재하지 않습니다."),

    //409 Conflict
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
    NICKNAME_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 닉네임입니다."),
    ERROR_SOCIAL_LOGIN_CONFLICT(HttpStatus.CONFLICT, "해당 소셜 계정은 이미 다른 계정에 연결되어 있습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;
}
