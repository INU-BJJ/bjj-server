package com.appcenter.BJJ.domain.notification.service;

import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmAsyncService fcmAsyncService;

    // FCM 전송
    public void sendMessage(Map<Long, List<NotificationInfoDto>> notificationInfoMap) {
        log.info("[로그] FcmService.sendMessage(), 대상 회원 수 = {}", notificationInfoMap.size());

        notificationInfoMap.forEach((memberId, infoList) -> {
            for (NotificationInfoDto info : infoList) {
                List<String> fcmTokens = info.getFcmTokens();
                if (fcmTokens.isEmpty()) continue;

                String title = "[밥점줘] 오늘의 메뉴 알림 \uD83C\uDF71";
                String body = String.format("%s님이 좋아하는 '%s', 오늘 %s %s에서 제공돼요!",
                        info.getMemberNickname(),
                        info.getMenuName(),
                        info.getCafeteriaName(),
                        info.getCafeteriaCorner());
                log.debug("[로그] 메시지: {}", body);

                MulticastMessage multicastMessage = MulticastMessage.builder()
                        .addAllTokens(fcmTokens)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();


                fcmAsyncService.sendMulticastWithRetry(multicastMessage, memberId, 0);
            }
        });
    }
}
