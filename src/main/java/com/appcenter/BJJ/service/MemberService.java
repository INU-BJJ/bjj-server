package com.appcenter.BJJ.service;

import com.appcenter.BJJ.domain.Member;
import com.appcenter.BJJ.dto.MemberResponseDTO;
import com.appcenter.BJJ.dto.SignupDTO;
import com.appcenter.BJJ.exception.CustomException;
import com.appcenter.BJJ.exception.ErrorCode;
import com.appcenter.BJJ.jwt.JwtProvider;
import com.appcenter.BJJ.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public String signUp(SignupDTO signupDTO) {
        log.info("MemberService-signup: 진입");
        Member member = memberRepository.findByEmailAndProvider(signupDTO.getEmail(), signupDTO.getProvider()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        member.update("ROLE_USER");
        memberRepository.save(member);
        log.info("MemberService-signup: ROLE_USER로 변경 완료 및 회원가입 성공");
        String accessToken = getToken(member.getProviderId(), JwtProvider.validAccessTime);

        return accessToken;
    }

    public MemberResponseDTO getMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_CREDENTIALS)
        );
        log.info("MemberService-getMember: 회원 정보 조회 성공");
        return MemberResponseDTO.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .provider(member.getProvider())
                .build();
    }

    public String getToken(String providerId, Long time) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(providerId, "");
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info("MemberService-getToken: 회원이 존재합니다.");
        String token = jwtProvider.generateToken(authentication, time);
        log.info("MemberService-getToken: 토큰 발급이 됐습니다.");
        return token;
    }
}
