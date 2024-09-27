package com.appcenter.BJJ.jwt;

import com.appcenter.BJJ.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
    public static final long validAccessTime = 60 * 60 * 3L;

    public JwtProvider(@Value("${spring.jwt.secret}") String secretKey, UserDetailsServiceImpl userDetailsService) {
        byte[] ketBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(ketBytes);
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(Authentication authentication, Long time) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining());
        Member member = ((UserDetailsImpl) authentication.getPrincipal()).getMember();
        long now = (new Date()).getTime();
        log.info("JWTProvider-generateToken: " + member.getEmail());

        return Jwts.builder()
                .setSubject(member.getProviderId())
                .claim("auth", authorities)
                .setExpiration(new Date(now + time))
                .signWith(key)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(""))
                .map(SimpleGrantedAuthority::new)
                .toList();
        UserDetailsImpl userDetails = userDetailsService.loadUserByProviderId(claims.getSubject());
        log.info("JwtProvider-getAuthentication: userDetails 가져옴 " + userDetails.getUsername());
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}