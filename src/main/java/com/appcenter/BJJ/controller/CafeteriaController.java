package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.service.CafeteriaData;
import com.appcenter.BJJ.service.CafeteriaService;
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
public class CafeteriaController {

    private final CafeteriaService cafeteriaService;

    @GetMapping
    public ResponseEntity<List<String>> getAllCafeteriaNamesWithoutDuplication() {
        List<String> cafeteriaNameList = Arrays.stream(CafeteriaData.values()).map(CafeteriaData::getName).toList();

        return ResponseEntity.ok(cafeteriaNameList);
    }
}
