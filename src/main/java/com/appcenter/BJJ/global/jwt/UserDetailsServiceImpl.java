package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final MemberRepository memberRepository;

    public UserDetailsImpl loadUserByProviderId(String providerId) throws UsernameNotFoundException {
        log.info("UserDetailServiceImpl.loadByUsername() - 토큰의 회원 인가 시작");
        Member member = memberRepository.findByProviderId(providerId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        log.info("UserDetailServiceImpl.loadUserByEmail() - 회원이 존재합니다. " + member.getEmail());
        return new UserDetailsImpl(member);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
