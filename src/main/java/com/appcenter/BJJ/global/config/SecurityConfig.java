package com.appcenter.BJJ.global.config;

import com.appcenter.BJJ.global.jwt.AuthenticationProviderImpl;
import com.appcenter.BJJ.global.jwt.JwtFilter;
import com.appcenter.BJJ.global.jwt.JwtProvider;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                //h2 접근을 위해
                .csrf(AbstractHttpConfigurer::disable)
                .headers(header -> header
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("swagger-ui/**", "v3/**", "/h2-console/**").permitAll()
                        .requestMatchers("api/member/sign-up/**", "api/member/check-nickname").permitAll()
                        .requestMatchers("oauth2/authorization/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

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
