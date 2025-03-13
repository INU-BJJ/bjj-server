package com.appcenter.BJJ.domain.menu.controller;

import com.appcenter.BJJ.domain.menu.dto.TodayDietRes;
import com.appcenter.BJJ.domain.menu.dto.TodayMenuRes;
import com.appcenter.BJJ.domain.menu.service.TodayDietService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/today-diets")
@RequiredArgsConstructor
@Tag(name = "TodayDiet", description = "오늘의 식단 API")
public class TodayDietController {

    private final TodayDietService todayDietService;

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
}
