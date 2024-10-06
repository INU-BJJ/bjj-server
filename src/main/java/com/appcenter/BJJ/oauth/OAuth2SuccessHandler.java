package com.appcenter.BJJ.oauth;

import com.appcenter.BJJ.jwt.JwtProvider;
import com.appcenter.BJJ.jwt.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    //로그인 성공 및 토큰 발급 및 redirect url
    @Value("${spring.server.host}")
    private String domain;
    @Value("${spring.oauth2.url.sign_in}")
    private String signInUrl;
    @Value("${spring.oauth2.url.sign_up}")
    private String signUpUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2SuccessHandler 진입");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtProvider.generateToken(authentication, JwtProvider.validAccessTime);
        String redirectUrl;
        if (userDetails.getMember().getRole().equals("ROLE_GUEST")) {
            log.info("OAuth2SuccessHandler-onAuthenticationSuccess: 회원가입으로 이동");
            redirectUrl = UriComponentsBuilder.fromHttpUrl(domain + signUpUrl)
                    .queryParam("email", userDetails.getMember().getEmail())
                    .queryParam("token", token).toUriString();
        } else {
            log.info("OAuth2SuccessHandler-onAuthenticationSuccess: 로그인으로 이동");
            redirectUrl = UriComponentsBuilder.fromHttpUrl(domain + signInUrl)
                    .queryParam("token", token).toUriString();
        }
        response.sendRedirect(redirectUrl);
        return;
    }
}



