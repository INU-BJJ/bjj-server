package com.appcenter.BJJ.global.oauth;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.domain.MemberReportBan;
import com.appcenter.BJJ.domain.member.domain.OAuth2Client;
import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
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
        // 내부적으로 토큰을 발급받고 사용자 정보 가져옴 (OAuth2 Authorization Code Grant Flow 기반으로 동작)
        log.info("OAuth2UserServiceExt.loadUser() - 진입");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(userRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());

        Member member = memberRepository.findByEmailAndProviderId(oAuth2UserInfo.getEmail(), oAuth2UserInfo.getProviderId()).orElseGet(
                () -> Member.builder()
                        .provider(oAuth2UserInfo.getProvider())
                        .providerId(oAuth2UserInfo.getProviderId())
                        .nickname(oAuth2UserInfo.getNickname())
                        .email(oAuth2UserInfo.getEmail())
                        .memberReportBan(MemberReportBan.create())
                        .oAuth2Client(OAuth2Client.builder()
                                .oauthToken(userRequest.getAccessToken().getTokenValue())
                                .issuedAt(userRequest.getAccessToken().getIssuedAt())
                                .expiresAt(userRequest.getAccessToken().getExpiresAt())
                                .build())
                        .build()
        );

        if (!memberRepository.existsByProviderId(member.getProviderId())) {
            log.info("OAuth2UserServiceExt.loadUser() - 신규 회원 이메일 : {}, 신규 회원 role : {}", member.getEmail(), member.getRole());
            memberRepository.save(member);
        } else {
            log.info("OAuth2UserServiceExt.loadUser() - 기존 회원 이메일 : {}, 기존 회원 role : {}", member.getEmail(), member.getRole());

            // loadUser시 재접속, 토큰 재발급됨
            member.updateOauthToken(OAuth2Client.builder()
                    .oauthToken(userRequest.getAccessToken().getTokenValue())
                    .issuedAt(userRequest.getAccessToken().getIssuedAt())
                    .expiresAt(userRequest.getAccessToken().getExpiresAt())
                    .build());
            memberRepository.save(member);
        }

        return new UserDetailsImpl(member); //authentication의 principal로 담겨진 채 successhandler로 이동
    }
}
