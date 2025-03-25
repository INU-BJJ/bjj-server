package com.appcenter.BJJ.domain.todaydiet.scheduler;

import com.appcenter.BJJ.domain.todaydiet.service.DietUpdateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class DietScheduler {

    private final DietUpdateService dietUpdateService;

    @PostConstruct
    @Scheduled(cron = "0 0 7 * * MON") // 매주 월요일 오전 7시 실행
    // 스케쥴링은 프록시 메소드가 적어도 protected 이어야 함
    protected void triggerWeeklyDietUpdate() throws IOException {
        dietUpdateService.fetchWeeklyDietInfo();
    }
}
