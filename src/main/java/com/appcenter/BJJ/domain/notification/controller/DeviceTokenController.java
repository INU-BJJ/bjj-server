package com.appcenter.BJJ.domain.notification.controller;

import com.appcenter.BJJ.domain.notification.service.DeviceTokenService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
@Tag(name = "DeviceToken", description = "FCM 기기 토큰 API")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    @Operation(summary = "FCM 기기 토큰 등록", description = "이미 등록된 기기 토큰 등록 시도 시 해당 기기 토큰의 ID 반환")
    public ResponseEntity<Long> postDeviceToken(String token, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/device-tokens, token: {}, memberNickname : {}", token, userDetails.getNickname());

        Long deviceTokenId = deviceTokenService.createDeviceToken(token, userDetails.getMember().getId());

        return ResponseEntity.ok(deviceTokenId);
    }
}