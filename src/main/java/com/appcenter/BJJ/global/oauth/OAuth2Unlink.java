
package com.appcenter.BJJ.global.oauth;

import com.appcenter.BJJ.domain.member.dto.MemberOAuthVO;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2Unlink {
    private final ClientRegistrationRepository clientRegistrationRepositor;
    private final OAuth2UserServiceExt oAuth2UserServiceExt;

    @Value("${spring.naver.client-id}")
    private String clientId;
    @Value("${spring.naver.client-secret}")
    private String clientSecret;
    @Value("${spring.kakao.app-key}")
    private String appKey;

    public Long unlinkHandler(MemberOAuthVO memberOAuthVO) {
        UserDetailsImpl userDetails = reloadUser(memberOAuthVO);

        memberOAuthVO = MemberOAuthVO.from(userDetails.getMember());
        switch (memberOAuthVO.getProvider()) {
            case "google" -> unlinkGoogle(memberOAuthVO.getOauthToken());
            case "naver" -> unlinkNaver(memberOAuthVO.getOauthToken());
            case "kakao" -> unlinkKakao(memberOAuthVO.getProviderId());
            default -> throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return memberOAuthVO.getMemberId();
    }

    private void unlinkGoogle(String oauthToken) {
        String googleURL = "https://oauth2.googleapis.com/revoke";
        Map response = RestClient.builder()
                .baseUrl(googleURL)
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("token", oauthToken)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new CustomException(ErrorCode.SERVER_ERROR);
                })
                .body(Map.class);

        log.info(String.valueOf(response));
    }

    private void unlinkNaver(String oauthToken) {
        String naverURL = "https://nid.naver.com/oauth2.0/token";
        Map response = RestClient.builder()
                .baseUrl(naverURL)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("grant_type", "delete")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("access_token", oauthToken)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new CustomException(ErrorCode.SERVER_ERROR);
                })
                .body(Map.class);

        log.info(String.valueOf(response));
    }

    // Admin 키로 연결해제
    private void unlinkKakao(String providerId) {
        String kakaoURL = "https://kapi.kakao.com/v1/user/unlink";
        Map response = RestClient.builder()
                .baseUrl(kakaoURL)
                //appkey임
                .defaultHeader("Authorization", "KakaoAK " + appKey)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("target_id_type", "user_id")
                        .queryParam("target_id", providerId)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new CustomException(ErrorCode.SERVER_ERROR);
                })
                .body(Map.class);

        log.info(String.valueOf(response));
    }

    private UserDetailsImpl reloadUser(MemberOAuthVO memberOAuthVO) {
        //reload 후 oauth 토큰 재발급
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, memberOAuthVO.getOauthToken(), memberOAuthVO.getIssuedAt(), memberOAuthVO.getExpiresAt());
        ClientRegistration clientRegistration = clientRegistrationRepositor.findByRegistrationId(memberOAuthVO.getProvider());

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, oAuth2AccessToken);
        return (UserDetailsImpl) oAuth2UserServiceExt.loadUser(userRequest);
    }
}
