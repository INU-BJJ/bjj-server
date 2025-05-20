package com.appcenter.BJJ.domain.notification.scheduler;

import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.appcenter.BJJ.domain.notification.service.DietNotificationService;
import com.appcenter.BJJ.domain.notification.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final DietNotificationService dietNotificationService;
    private final FcmService fcmService;

    @Scheduled(cron = "0 30 7 * * *") // 매일 오전 7시 30분
    protected void sendDietNotifications() {
        LocalDate today = LocalDate.now();
        log.info("[로그] {} 식단 메뉴 알림 스케줄러 시작", today);

        List<NotificationInfoDto> notificationTargets =
                dietNotificationService.collectNotificationTargets(today);

        fcmService.sendMessage(notificationTargets);

        log.info("[로그] {} 식단 메뉴 알림 스케줄러 종료", today);
    }

}
