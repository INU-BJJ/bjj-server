package com.appcenter.BJJ.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class ErrorDTO {
    private String code;
    private List<String> msg;

    //custom error의 response
    public static ResponseEntity<ErrorDTO> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorDTO.builder()
                        .code(errorCode.getCode())
                        .msg(Collections.singletonList(errorCode.getMessage()))
                        .build());
    }

    //valid error의 response
    public static ResponseEntity<ErrorDTO> toResponseEntity(ErrorCode errorCode, List<FieldError> errorList) {
        List<String> msgList = errorList.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorDTO.builder()
                        .code(errorCode.getCode())
                        .msg(msgList)
                        .build());
    }
}
