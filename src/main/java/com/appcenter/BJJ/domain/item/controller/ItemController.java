package com.appcenter.BJJ.domain.item.controller;

import com.appcenter.BJJ.domain.item.dto.DetailItemRes;
import com.appcenter.BJJ.domain.item.dto.MyItemRes;
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

    @Operation(summary = "마이페이지 아이템 조회")
    @GetMapping("/my")
    public ResponseEntity<MyItemRes> getMyItem(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(itemService.getMyItem(userDetails.getMember().getId()));
    }

    @Operation(summary = "아이템 뽑기")
    @PostMapping
    public ResponseEntity<DetailItemRes> gachaItem(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam ItemType itemType) {
        return ResponseEntity.ok(itemService.gacha(userDetails.getMember().getId(), itemType));
    }

    @Operation(summary = "전체 아이템 조회", description = "날짜 == 현재 : 안 뽑힌 아이템 / 날짜 != 현재 : 뽑힌 아이템")
    @GetMapping
    public ResponseEntity<List<DetailItemRes>> getItems(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam ItemType itemType) {
        return ResponseEntity.ok(itemService.getItems(userDetails.getMember().getId(), itemType));
    }

    @Operation(summary = "개별 아이템 조회", description = "날짜 == 현재 : 안 뽑힌 아이템 / 날짜 != 현재 : 뽑힌 아이템")
    @GetMapping("/{itemId}")
    public ResponseEntity<DetailItemRes> getItem(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long itemId, @RequestParam ItemType itemType) {
        return ResponseEntity.ok(itemService.getItem(userDetails.getMember().getId(), itemId, itemType));
    }

    @Operation(summary = "아이템 착용", description = "기본아이템의 경우, dto의 모든 필드 값 == null")
    @PatchMapping("/{itemId}")
    //TODO 배경에 대한 착용도 추가하기
    public void updateIsWearing(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam ItemType itemType, @PathVariable Long itemId) {
        itemService.toggleIsWearing(userDetails.getMember().getId(), itemType, itemId);
    }
}
