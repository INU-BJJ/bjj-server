package com.appcenter.BJJ.domain.todaydiet.controller;

import com.appcenter.BJJ.domain.notification.dto.NotificationInfoDto;
import com.appcenter.BJJ.domain.notification.service.DietNotificationService;
import com.appcenter.BJJ.domain.notification.service.FcmService;
import com.appcenter.BJJ.domain.todaydiet.dto.TodayDietRes;
import com.appcenter.BJJ.domain.todaydiet.dto.TodayMenuRes;
import com.appcenter.BJJ.domain.todaydiet.service.TodayDietService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/today-diets")
@RequiredArgsConstructor
@Tag(name = "TodayDiet", description = "오늘의 식단 API")
public class TodayDietController {

    private final TodayDietService todayDietService;
    private final DietNotificationService dietNotificationService;
    private final FcmService fcmService;

    @Operation(summary = "특정 식당에서 오늘의 식단 목록 조회",
            description = """
                    - cafeteriaName: 식당명
                    - 특정 식당에서 모든 코너의 오늘의 식단 정보 조회
                    """)
    @GetMapping
    public ResponseEntity<List<TodayDietRes>> getTodayDietsByCafeteriaName(String cafeteriaName, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/today-diets?cafeteriaName={}", cafeteriaName);

        List<TodayDietRes> todayDietList = todayDietService.findByCafeteria(cafeteriaName, userDetails.getMember().getId());

        return ResponseEntity.ok(todayDietList);
    }

    @Operation(summary = "특정 식당에서 오늘의 메인 메뉴 목록 조회",
            description = """
                    - cafeteriaName: 식당명
                    - 특정 식당에서 모든 코너의 오늘의 식단 중 메인 메뉴 정보 조회
                    """)
    @GetMapping("/main-menus")
    public ResponseEntity<List<TodayMenuRes>> getTodayMainMenusByCafeteriaName(String cafeteriaName) {
        log.info("[로그] GET /api/today-diets/main-menus?cafeteriaName={}", cafeteriaName);

        List<TodayMenuRes> todayMainMenuList = todayDietService.findMainMenusByCafeteria(cafeteriaName);

        return ResponseEntity.ok(todayMainMenuList);
    }

    @PostMapping("/test/notification")
    @Operation(summary = "[test] 오늘 식단 메뉴에 좋아요를 누른 회원에게 알림 일괄 전송")
    public ResponseEntity<Void> testDietNotification() {
        log.info("[로그] POST /api/device-tokens/test/notification");

        Map<Long, List<NotificationInfoDto>> notificationTargets = dietNotificationService.collectNotificationTargets(LocalDate.now());
        fcmService.sendMessage(notificationTargets);

        return ResponseEntity.noContent().build();
    }
}
