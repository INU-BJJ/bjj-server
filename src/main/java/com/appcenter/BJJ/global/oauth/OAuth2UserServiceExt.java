package com.appcenter.BJJ.global.oauth;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import com.appcenter.BJJ.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceExt extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2UserServiceExt.loadUser() - 진입");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(userRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        log.info("OAuth2UserServiceExt.loadUser() - oAuth2 정보 가져옴 : {}", oAuth2UserInfo.getEmail());

        Member member = memberRepository.findByEmailAndProviderId(oAuth2UserInfo.getEmail(), oAuth2UserInfo.getProviderId()).orElseGet(
                () -> Member.builder()
                        .provider(oAuth2UserInfo.getProvider())
                        .providerId(oAuth2UserInfo.getProviderId())
                        .nickname(oAuth2UserInfo.getNickname())
                        .email(oAuth2UserInfo.getEmail())
                        .build()
        );
        memberRepository.save(member);
        log.info("OAuth2UserServiceExt.loadUser() - member 저장 : {}", member.getRole());

        return new UserDetailsImpl(member); //authentication의 principal로 담겨진 채 successhandler로 이동
    }
}
