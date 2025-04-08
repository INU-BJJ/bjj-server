package com.appcenter.BJJ.domain.todaydiet.scheduler;

import com.appcenter.BJJ.domain.todaydiet.service.DietUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DietScheduler {

    private final DietUpdateService dietUpdateService;

    @Order(2) // 숫자가 낮을수록 먼저 실행
    @EventListener(ApplicationReadyEvent.class) // Spring 애플리케이션이 완전히 실행된 후 실행 (@PostConstruct 이후)
    @Scheduled(cron = "0 0 7 * * MON") // 매주 월요일 오전 7시 실행
    // 스케쥴링은 프록시 메소드가 적어도 protected 이어야 함
    protected void triggerWeeklyDietUpdate() throws IOException {
        dietUpdateService.updateWeeklyDietWithRetry();
    }
}
