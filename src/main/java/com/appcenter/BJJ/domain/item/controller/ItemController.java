package com.appcenter.BJJ.domain.item.controller;

import com.appcenter.BJJ.domain.item.dto.DetailItemRes;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.service.ItemService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "아이템 API")
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "아이템 뽑기")
    @PostMapping
    public ResponseEntity<DetailItemRes> gachaItem(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam ItemType itemType) {
        return ResponseEntity.ok(itemService.gacha(userDetails.getMember().getId(), itemType));
    }

    @Operation(summary = "전체 아이템 조회", description = "날짜 == 현재 : 안 뽑힌 아이템 / 날짜 != 현재 : 뽑힌 아이템")
    @GetMapping
    public ResponseEntity<List<DetailItemRes>> getItems(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(itemService.getItems(userDetails.getMember().getId()));
    }

    @Operation(summary = "개별 아이템 조회", description = "날짜 == 현재 : 안 뽑힌 아이템 / 날짜 != 현재 : 뽑힌 아이템")
    @GetMapping("/{itemId}")
    public ResponseEntity<DetailItemRes> getItem(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Integer itemId) {
        return ResponseEntity.ok(itemService.getItem(userDetails.getMember().getId(), itemId));
    }
}
