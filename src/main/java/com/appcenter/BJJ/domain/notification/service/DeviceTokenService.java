package com.appcenter.BJJ.domain.notification.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.notification.domain.DeviceToken;
import com.appcenter.BJJ.domain.notification.repository.DeviceTokenRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createDeviceToken(String token, Long memberId) {
        log.info("[로그] createDeviceToken(), token : {}, memberId : {}", token, memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 동일한 기기 토큰이 등록된 경우 해당 토큰의 ID 반환
        Optional<DeviceToken> optionalDeviceToken = deviceTokenRepository.findByToken(token);
        if (optionalDeviceToken.isPresent()) {
            return optionalDeviceToken.get().getId();
        }

        DeviceToken deviceToken = DeviceToken.builder()
                .member(member)
                .token(token)
                .build();

        return deviceTokenRepository.save(deviceToken).getId();
    }

    @Transactional
    public void deactivateTokens(Set<String> tokens) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByTokenIn(new ArrayList<>(tokens));
        deviceTokens.forEach(DeviceToken::deactivate);
    }

    @Transactional
    public void updateLastUsedAt(Set<String> tokenValues) {
        List<DeviceToken> tokens = deviceTokenRepository.findAllByTokenIn(new ArrayList<>(tokenValues));
        LocalDateTime now = LocalDateTime.now();
        tokens.forEach(token -> token.updateLastUsedAt(now));
    }
}