package com.appcenter.BJJ.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //400 Bad Request
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "400-1", "입력값이 올바르지 않습니다."),
    CANNOT_LIKE_OWN_REVIEW(HttpStatus.BAD_REQUEST, "400-2", "자신의 리뷰에는 좋아요를 누를 수 없습니다."),
    NOT_ENOUGH_POINTS(HttpStatus.BAD_REQUEST, "400-3", "포인트가 충분하지 않습니다."),

    //401 Unauthorized
    ERROR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401-1", "인증 정보가 없습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "401-2", "토큰의 형식이 유효하지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "401-3", "토큰 기간이 만료됐습니다."),

    //403 Forbidden
    ERROR_FORBIDDEN(HttpStatus.FORBIDDEN, "403-1", "이 리소스에 접근할 권한이 없습니다."),
    MEMBER_SUSPENDED_FOR_REVIEW(HttpStatus.FORBIDDEN, "403-2", "리뷰 작성이 제한된 사용자입니다."),

    //404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "404-1", "해당 회원이 존재하지 않습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "404-2", "해당 아이템이 존재하지 않습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "404-3", "리뷰가 삭제되었거나 존재하지 않는 리뷰입니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "404-4", "해당 메뉴가 존재하지 않습니다."),
    REVIEW_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "404-5", "해당 리뷰의 상세 정보를 불러올 수 없습니다"),
    CAFETERIA_NOT_FOUND(HttpStatus.NOT_FOUND, "404-6", "해당 식당이 존재하지 않습니다"),

    //409 Conflict
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "409-1", "이미 등록된 이메일입니다."),
    NICKNAME_ALREADY_REGISTERED(HttpStatus.CONFLICT, "409-2", "이미 등록된 닉네임입니다."),
    ERROR_SOCIAL_LOGIN_CONFLICT(HttpStatus.CONFLICT, "409-3", "해당 소셜 계정은 이미 다른 계정에 연결되어 있습니다."),
    DUPLICATE_REPORT(HttpStatus.CONFLICT, "409-4", "이미 신고한 리뷰입니다."),

    //500 Internal Server Error
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500-1", "알 수 없는 문제가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
