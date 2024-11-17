package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.exception.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtValidateFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException |
                 IllegalArgumentException | NullPointerException e) {
            log.warn("JwtValidateFilter.doFilterInternal() - INVALID_TOKEN_FORMAT 예외전파됨");
            setErrorResponse(response, ErrorCode.INVALID_TOKEN_FORMAT);
        } catch (ExpiredJwtException e) {
            log.warn("JwtValidateFilter.doFilterInternal() - EXPIRED_TOKEN 예외전파됨");
            setErrorResponse(response, ErrorCode.EXPIRED_TOKEN);
        }
    }

    private void setErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(ErrorDTO.builder().msg(errorCode.getMessage()).build()));
        } catch (IOException e) {
            log.warn("IOException 발생");
        }
    }
}
