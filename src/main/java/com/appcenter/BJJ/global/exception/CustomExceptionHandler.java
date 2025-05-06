package com.appcenter.BJJ.global.exception;

import com.appcenter.BJJ.global.exception.dto.ErrorDTO;
import com.appcenter.BJJ.global.exception.dto.ReviewSuspensionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
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

}
