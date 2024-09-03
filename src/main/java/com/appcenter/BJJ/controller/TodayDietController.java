package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.dto.TodayDietRes;
import com.appcenter.BJJ.service.TodayDietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/today-diets")
@RequiredArgsConstructor
public class TodayDietController {

    private final TodayDietService todayDietService;

    @GetMapping
    public ResponseEntity<List<TodayDietRes>> getTodayDietsByCafeteriaName(String cafeteriaName) {
        List<TodayDietRes> todayDietList = todayDietService.findByCafeteria(cafeteriaName);

        return ResponseEntity.ok(todayDietList);
    }
}
