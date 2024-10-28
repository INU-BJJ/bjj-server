package com.appcenter.BJJ.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorDTO> handleCustomException(CustomException e) {
        return ErrorDTO.toResponseEntity(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<List<ErrorDTO>> handleValidationExceptions(MethodArgumentNotValidException e) {
        List<FieldError> errorList = e.getBindingResult().getFieldErrors();
        List<ErrorDTO> responseError = new ArrayList<>();

        for (FieldError error : errorList) {
            ErrorDTO errorDTO = ErrorDTO.builder()
                    .msg(error.getDefaultMessage())
                    .build();
            responseError.add(errorDTO);
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(responseError);
    }
}
