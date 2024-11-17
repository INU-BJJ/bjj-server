package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.exception.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // 처리하지 않은 권한 관련 오류들은 모두 이 곳으로 흘러와서 처리됨
        log.warn("AccessDeniedHandlerImpl.handle() : 해당 회원에게 권한이 없음");
        // 권한 관련 errorcode 만들기
        ErrorCode errorCode = ErrorCode.ERROR_FORBIDDEN;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorDTO.builder().msg(errorCode.getMessage()).build()));
    }
}
