package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.domain.member.domain.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {
    private final UserDetailsServiceImpl userDetailsService;
    private final Key key;
    public static final long validAccessTime = 2L * 365 * 24 * 60 * 60 * 1000; // 2년 (이후에 3달로 바꿀 예정)

    private JwtProvider(@Value("${spring.jwt.secret}") String secretKey, UserDetailsServiceImpl userDetailsService) {
        byte[] ketBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(ketBytes);
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(Authentication authentication, Long time) {
        log.info("JWTProvider.generateToken() - 토큰 만들기 시작");
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining());
        Member member = ((UserDetailsImpl) authentication.getPrincipal()).getMember();
        long now = (new Date()).getTime();
        log.info("JWTProvider.generateToken() - 사용자 : {}", member.getEmail());

        String accessToken = Jwts.builder()
                .setSubject(member.getProviderId())
                .claim("auth", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(now + time))
                .signWith(key)
                .compact();
        log.info("JWTProvider.generateToken() - 토큰 발급 완료 / 발급일자 : {}, 유효일자 : {}", parseClaims(accessToken).getIssuedAt(), parseClaims(accessToken).getExpiration());
        return accessToken;
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(""))
                .map(SimpleGrantedAuthority::new)
                .toList();
        UserDetailsImpl userDetails = userDetailsService.loadUserByProviderId(claims.getSubject());
        log.info("JwtProvider.getAuthentication() - userDetails 가져옴 : {}", userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("토큰이 형식에 맞지 않음", e);
        } catch (ExpiredJwtException e) {
            log.info("토큰 유효 기간 지남", e);
        } catch (UnsupportedJwtException e) {
            log.info("지원하지 않는 토큰", e);
        } catch (IllegalArgumentException e) {
            log.info("토큰 claims 비어있음", e);
        } catch (NullPointerException e) {
            log.info("토큰 값이 null", e);
        }
        return false;
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
