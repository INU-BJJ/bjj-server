package com.appcenter.BJJ.domain.member.service;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.dto.MemberRes;
import com.appcenter.BJJ.domain.member.dto.SignupReq;
import com.appcenter.BJJ.domain.member.repository.MemberRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public String signUp(SignupReq signupReq) {
        log.info("MemberService.signup() - 진입");
        isNicknameAvailable(signupReq.getNickname());

        Member member = memberRepository.findByEmailAndProvider(signupReq.getEmail(), signupReq.getProvider()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        member.updateMemberInfo(signupReq.getNickname(), "ROLE_USER");
        memberRepository.save(member);
        log.info("MemberService.signup() - ROLE_USER로 변경 완료 및 회원가입 성공");

        String accessToken = getToken(member.getProviderId(), JwtProvider.validAccessTime);
        log.info("MemberService.signup() - 토큰 발급 성공");
        return accessToken;
    }

    public MemberRes getMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.INVALID_CREDENTIALS)
        );
        log.info("MemberService.getMember() - 회원 정보 조회 성공");

        return MemberRes.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .provider(member.getProvider())
                .build();
    }

    public boolean isNicknameAvailable(String nickname) {
        if (!memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_REGISTERED);
        }
        return true;
    }

    public String changeNickname(String currentNickname, String newNickname) {
        isNicknameAvailable(newNickname);

        Member member = memberRepository.findByNickname(currentNickname).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        member.updateNickname(newNickname);
        memberRepository.save(member);
        return newNickname;
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
