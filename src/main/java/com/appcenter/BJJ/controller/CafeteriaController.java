package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.service.CafeteriaData;
import com.appcenter.BJJ.service.CafeteriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/cafeterias")
@RequiredArgsConstructor
@Tag(name = "Cafeteria", description = "식당 API")
public class CafeteriaController {

    private final CafeteriaService cafeteriaService;

    @Operation(summary = "모든 식당 정보 조희")
    @GetMapping
    public ResponseEntity<List<String>> getAllCafeteriaNamesWithoutDuplication() {
        List<String> cafeteriaNameList = Arrays.stream(CafeteriaData.values()).map(CafeteriaData::getName).toList();

        return ResponseEntity.ok(cafeteriaNameList);
    }
}
