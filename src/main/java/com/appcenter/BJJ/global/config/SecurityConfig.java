package com.appcenter.BJJ.global.config;

import com.appcenter.BJJ.global.jwt.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationProviderImpl authenticationProvider;
    private final AuthenticationEntryPointImpl authenticationEntryPointImpl;
    private final AccessDeniedHandlerImpl accessDeniedHandlerImpl;
    private final TraceIdFilter traceIdFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //Todo hasRole로 api에 대한 권한 설정하기
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                //h2 접근을 위해
                .csrf(AbstractHttpConfigurer::disable)
                .headers(header -> header
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .authorizeHttpRequests(authorize -> authorize
                        //Todo test 관련된 거 나중에 없애기
                        .requestMatchers("/api/members/sign-up", "/api/members/socialLogin", "/api/members/check-nickname", "/api/members/test/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/fonts/**", "/images/**").permitAll()
                        .requestMatchers("/policy/**").permitAll()
                        .requestMatchers("/actuator/**")
                        .access((auth, ctx) -> {
                            String ip = ctx.getRequest().getRemoteAddr();
                            InetAddress inetAddress;
                            try {
                                inetAddress = InetAddress.getByName(ip);
                            } catch (UnknownHostException e) {
                                log.warn("[로그] 유효하지 않은 IP 주소: {}", ip, e);
                                return new AuthorizationDecision(false);
                            }

                            boolean isLocal = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress();
                            return new AuthorizationDecision(
                                    isLocal ||              // 개발 localhost
                                            ip.equals("172.30.0.5") // 운영 Prometheus
                            );
                        })
                        .anyRequest().authenticated())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtFilter(jwtProvider, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtValidateFilter(), JwtFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPointImpl)
                        .accessDeniedHandler(accessDeniedHandlerImpl));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        // spring security 무시
        return (web) -> web.ignoring().requestMatchers("/favicon.ico", "/firebase-messaging-sw.js")
                .requestMatchers("/swagger-ui/**", "/v3/**", "/h2-console/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        return authenticationConfiguration.getAuthenticationManager();
    }
}
