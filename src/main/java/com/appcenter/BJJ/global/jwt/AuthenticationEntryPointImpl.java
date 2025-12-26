package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.exception.dto.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 처리하지 않은 인증 관련 오류들은 모두 이 곳으로 흘러와서 처리됨

        // Forward 또는 Include된 경우 원래 URI 가져오기
        String originalUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (originalUri == null) {
            originalUri = (String) request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI);
        }
        if (originalUri == null) {
            originalUri = request.getRequestURI();
        }

        log.warn("[로그] 회원이 인증되지 않음 [URI: {}]", originalUri, authException);

        ErrorCode errorCode = ErrorCode.ERROR_UNAUTHORIZED;
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorDTO.builder()
                .code(errorCode.getCode())
                .msg(Collections.singletonList(errorCode.getMessage()))
                .build()));
    }
}
