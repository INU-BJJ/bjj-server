package com.appcenter.BJJ.global.exception;

import com.appcenter.BJJ.global.exception.dto.ErrorDTO;
import com.appcenter.BJJ.global.exception.dto.ReviewSuspensionDTO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    private static final String MEMBER_PROVIDER_IDENTITY = "uk_member_provider_identity";

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorDTO> handleCustomException(CustomException e) {
        return ErrorDTO.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorDTO> handleValidationExceptions(MethodArgumentNotValidException e) {
        return ErrorDTO.toResponseEntity(ErrorCode.INVALID_INPUT, e.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(ReviewSuspensionException.class)
    protected ResponseEntity<ReviewSuspensionDTO> handleReportException(ReviewSuspensionException e) {
        return ReviewSuspensionDTO.toResponseEntity(ErrorCode.MEMBER_SUSPENDED_FOR_REVIEW, e.getStartAt(), e.getEndAt());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorDTO> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) { //DataIntegrityViolationException 내부의 ConstraintViolationException 찾기
                String name = violation.getConstraintName();
                if (name != null) {
                    name = name.substring(name.lastIndexOf('.') + 1);
                    if (MEMBER_PROVIDER_IDENTITY.equalsIgnoreCase(name)) {
                        return ErrorDTO.toResponseEntity(ErrorCode.ACCOUNT_ALREADY_REGISTERED);
                    }
                }
            }
        }

        return ErrorDTO.toResponseEntity(ErrorCode.SERVER_ERROR);
    }
}
