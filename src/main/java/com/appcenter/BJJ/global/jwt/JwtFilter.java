package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.exception.dto.ErrorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer")) {
            String token = header.substring(7);
            // 토큰이 유효하지 않으면 jwtvalidatefilter로예외  전파
            try {
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException |
                     IllegalArgumentException | NullPointerException e) {
                log.warn("JwtFilter.doFilterInternal() - 토큰이 형식에 맞지 않음", e);
                throw e;
            } catch (ExpiredJwtException e) {
                log.warn("JwtFilter.doFilterInternal() - 토큰 유효 기간 지남", e);
                throw e;
            } catch (CustomException e) {
                ErrorCode errorCode = e.getErrorCode();
                response.setStatus(errorCode.getHttpStatus().value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(ErrorDTO.builder()
                        .code(errorCode.getCode())
                        .msg(Collections.singletonList(errorCode.getMessage()))
                        .build()));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
