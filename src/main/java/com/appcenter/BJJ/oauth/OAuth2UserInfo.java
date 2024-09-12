package com.appcenter.BJJ.oauth;

import com.appcenter.BJJ.exception.CustomException;
import com.appcenter.BJJ.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class OAuth2UserInfo {
    private String provider;
    private String providerId;
    private String email;
    private String nickname;

    public static OAuth2UserInfo of(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> ofGoogle(attributes);
            case "naver" -> ofNaver(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new CustomException(ErrorCode.USER_NOT_FOUND);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .provider("google")
                .providerId(String.valueOf(attributes.get("sub")))
                .email(String.valueOf(attributes.get("email")))
                .nickname(String.valueOf(attributes.get("name")))
                .build();
    }

    private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("response");
        return OAuth2UserInfo.builder()
                .provider("naver")
                .providerId(String.valueOf(account.get("id")))
                .email(String.valueOf(account.get("email")))
                .nickname(String.valueOf(account.get("name")))
                .build();

    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        return OAuth2UserInfo.builder()
                .provider("kakao")
                .providerId(String.valueOf(attributes.get("id")))
                .email(String.valueOf(account.get("email")))
                .nickname(String.valueOf(attributes.get("properties")))
                .build();
    }
}
