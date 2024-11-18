package com.appcenter.BJJ.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorDTO {
    private String msg;

    public static ResponseEntity<ErrorDTO> toResponseEntity(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorDTO.builder()
                        .msg(errorCode.getMessage())
                        .build());
    }
}
