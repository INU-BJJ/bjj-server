package com.appcenter.BJJ.global.config;

import com.appcenter.BJJ.global.jwt.*;
import com.appcenter.BJJ.global.oauth.OAuth2SuccessHandler;
import com.appcenter.BJJ.global.oauth.OAuth2UserServiceExt;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationProviderImpl authenticationProvider;
    private final OAuth2UserServiceExt oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // [notice] hasRole로 api에 대한 권한 설정하기 //
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                //h2 접근을 위해
                .csrf(AbstractHttpConfigurer::disable)
                .headers(header -> header
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("swagger-ui/**", "v3/**", "/h2-console/**").permitAll()
                        .requestMatchers("api/members/sign-up/**", "api/members/test/**", "api/members/check-nickname").permitAll()
                        .requestMatchers("oauth2/authorization/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtValidateFilter(), JwtFilter.class)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        http.oauth2Login(oauth -> oauth
                //사용자 정보를 가져오기 위한 함수
                .userInfoEndpoint(point -> point.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        return authenticationConfiguration.getAuthenticationManager();
    }
}
