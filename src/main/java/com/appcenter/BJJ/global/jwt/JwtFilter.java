package com.appcenter.BJJ.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer")) {
            String token = header.substring(7);
            // 토큰이 유효하지 않으면 jwtvalidatefilter로 예외 전파
            try {
                Authentication authentication = jwtProvider.getAuthentication(token);
                log.info("JwtFilter.doFilterInternal() - token 있음 & 인증 완료");
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException | UnsupportedJwtException |
                     IllegalArgumentException | NullPointerException e) {
                log.info("토큰이 형식에 맞지 않음", e);
                throw e;
            } catch (ExpiredJwtException e) {
                log.info("토큰 유효 기간 지남", e);
                throw e;
            }
        }
        filterChain.doFilter(request, response);
    }
}
