package com.appcenter.BJJ.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@Builder
@Slf4j
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
