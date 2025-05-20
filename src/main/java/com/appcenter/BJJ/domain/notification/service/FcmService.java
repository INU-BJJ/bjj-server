package com.appcenter.BJJ.domain.notification.service;

import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private static final int MAX_TOKENS_PER_BATCH = 500;

    private final FcmAsyncService fcmAsyncService;

    // FCM 전송
    public void sendMessage(List<NotificationInfoDto> notificationInfos) {
        log.info("[로그] FcmService.sendMessage(), 대상 메뉴 수 = {}", notificationInfos.size());

        for (NotificationInfoDto info : notificationInfos) {
            List<String> tokens = info.getFcmTokens();
            if (tokens.isEmpty()) continue;

            String title = "[밥점줘] 오늘의 메뉴 알림 \uD83C\uDF71";
            String body = String.format("회원님이 좋아하는 '%s', 오늘 %s %s에서 제공돼요!",
                    info.getMenuName(),
                    info.getCafeteriaName(),
                    info.getCafeteriaCorner());

            sendToBatchedTokens(tokens, title, body, info.getMenuName());
        }
    }

    private void sendToBatchedTokens(List<String> tokens, String title, String body, String menuName) {
        for (int i = 0; i < tokens.size(); i += MAX_TOKENS_PER_BATCH) {
            List<String> batchTokens = tokens.subList(i, Math.min(tokens.size(), i + MAX_TOKENS_PER_BATCH));

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batchTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            fcmAsyncService.sendMulticastWithRetry(message, menuName, 0);

        }
    }
}
