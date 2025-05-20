package com.appcenter.BJJ.domain.notification.service;

import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private static final int MAX_TOKENS_PER_BATCH = 500;

    private final FcmAsyncService fcmAsyncService;
    private final DeviceTokenService deviceTokenService;

    // FCM 전송
    public void sendMessage(List<NotificationInfoDto> notificationInfos) {
        log.info("[로그] FcmService.sendMessage(), 대상 메뉴 수 = {}", notificationInfos.size());

        List<CompletableFuture<List<String>>> futures = new ArrayList<>();

        for (NotificationInfoDto info : notificationInfos) {
            List<String> tokens = info.getFcmTokens();
            if (tokens.isEmpty()) continue;

            String title = "[밥점줘] 오늘의 메뉴 알림 \uD83C\uDF71";
            String body = String.format("회원님이 좋아하는 '%s', 오늘 %s %s에서 제공돼요!",
                    info.getMenuName(),
                    info.getCafeteriaName(),
                    info.getCafeteriaCorner());

            sendToBatchedTokens(tokens, title, body, info.getMenuName(), futures);
        }

        // 모든 비동기 작업 완료 후 실패 토큰 집계
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Set<String> allInvalidTokens = futures.stream()
                .flatMap(future -> future.join().stream())
                .collect(Collectors.toSet());

        if (!allInvalidTokens.isEmpty()) {
            log.info("[로그] 유효하지 않은 FCM 토큰 {}개 비활성화", allInvalidTokens.size());
            deviceTokenService.deactivateTokens(allInvalidTokens);
        }
    }

    private void sendToBatchedTokens(List<String> tokens, String title, String body, String menuName, List<CompletableFuture<List<String>>> futures) {
        for (int i = 0; i < tokens.size(); i += MAX_TOKENS_PER_BATCH) {
            List<String> batchTokens = tokens.subList(i, Math.min(tokens.size(), i + MAX_TOKENS_PER_BATCH));

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batchTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            futures.add(fcmAsyncService.sendMulticastWithRetry(message, batchTokens, menuName, 0));
        }
    }
}
