package com.appcenter.BJJ.domain.item.controller;

import com.appcenter.BJJ.domain.item.dto.GachaRes;
import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.service.InventoryService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "인벤토리 API")
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(summary = "아이템 뽑기")
    @PostMapping
    public ResponseEntity<GachaRes> gotchaItem(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam ItemType itemType) {
        return ResponseEntity.ok(inventoryService.gacha(userDetails.getMember().getId(), itemType));
    }
}
