package com.appcenter.BJJ.global.exception.dto;

import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewSuspensionDTO {
    private String code;
    private String msg;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public static ResponseEntity<ReviewSuspensionDTO> toResponseEntity(ErrorCode errorCode, LocalDateTime startAt, LocalDateTime endAt) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ReviewSuspensionDTO.builder()
                        .code(errorCode.getCode())
                        .msg(errorCode.getMessage())
                        .startAt(startAt)
                        .endAt(endAt)
                        .build());
    }
}
