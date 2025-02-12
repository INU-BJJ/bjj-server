package com.appcenter.BJJ.domain.menu.controller;

import com.appcenter.BJJ.domain.menu.domain.CafeteriaData;
import com.appcenter.BJJ.domain.menu.dto.CafeteriaInfoRes;
import com.appcenter.BJJ.domain.menu.service.CafeteriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cafeterias")
@RequiredArgsConstructor
@Tag(name = "Cafeteria", description = "식당 API")
public class CafeteriaController {

    private final CafeteriaService cafeteriaService;

    @Operation(summary = "모든 식당명 조회")
    @GetMapping
    public ResponseEntity<List<String>> getAllCafeteriaNamesWithoutDuplication() {
        log.info("[로그] GET /api/cafeterias");

        List<String> cafeteriaNameList = Arrays.stream(CafeteriaData.values()).map(CafeteriaData::getName).toList();

        return ResponseEntity.ok(cafeteriaNameList);
    }

    @Operation(summary = "특정 식당 정보 조회")
    @GetMapping("/{name}")
    public ResponseEntity<CafeteriaInfoRes> getCafeteriaByName(@PathVariable String name) {
        log.info("[로그] GET /api/cafeterias/{}", name);

        CafeteriaInfoRes cafeteriaInfo = cafeteriaService.findCafeteriaInfoByName(name);
        
        return ResponseEntity.ok(cafeteriaInfo);
    }
}
