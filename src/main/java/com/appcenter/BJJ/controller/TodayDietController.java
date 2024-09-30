package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.dto.TodayDietRes;
import com.appcenter.BJJ.dto.TodayMenuRes;
import com.appcenter.BJJ.service.TodayDietService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/today-diets")
@RequiredArgsConstructor
@Tag(name = "TodayDiet", description = "오늘의 식단 API")
public class TodayDietController {

    private final TodayDietService todayDietService;

    @Operation(summary = "모든 오늘의 식단 조회",
            description = "식당별 모든 식단에 대한 정보 조회")
    @GetMapping
    public ResponseEntity<List<TodayDietRes>> getTodayDietsByCafeteriaName(String cafeteriaName) {
        List<TodayDietRes> todayDietList = todayDietService.findByCafeteria(cafeteriaName);

        return ResponseEntity.ok(todayDietList);
    }

    @Operation(summary = "오늘의 식단 메인 메뉴 조회",
            description = "식당별 오늘의 식단 중 메인 메뉴에 대한 정보 조회")
    @GetMapping("/main-menus")
    public ResponseEntity<List<TodayMenuRes>> getTodayMainMenusByCafeteriaName(String cafeteriaName) {
        List<TodayMenuRes> todayMainMenuList = todayDietService.findMainMenusByCafeteria(cafeteriaName);

        return ResponseEntity.ok(todayMainMenuList);
    }
}
