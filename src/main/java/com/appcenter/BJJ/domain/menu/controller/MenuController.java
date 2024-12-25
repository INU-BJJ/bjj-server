package com.appcenter.BJJ.domain.menu.controller;

import com.appcenter.BJJ.domain.menu.dto.MenuRes;
import com.appcenter.BJJ.domain.menu.service.MenuLikeService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "메뉴 API")
public class MenuController {

    private final MenuLikeService menuLikeService;

    @PostMapping("/{menuId}/likes")
    @Operation(summary = "메뉴 좋아요")
    public ResponseEntity<Long> likeMenu(@PathVariable long menuId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/menus/{}/likes", menuId);

        long menuLikeId = menuLikeService.addLikeToMenu(menuId, userDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(menuLikeId);
    }

    @DeleteMapping("/{menuId}/likes")
    @Operation(summary = "메뉴 좋아요 취소")
    public ResponseEntity<Void> unlikeMenu(@PathVariable long menuId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] DELETE /api/menus/{}/likes", menuId);

        menuLikeService.removeLikeFromMenu(menuId, userDetails.getMember().getId());

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/liked")
    @Operation(summary = "회원이 좋아요 누른 메뉴 조회")
    public ResponseEntity<List<MenuRes>> getLikedMenus(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/menus/liked");

        List<MenuRes> likedMenuList = menuLikeService.getLikedMenus(userDetails.getMember().getId());

        return ResponseEntity.ok(likedMenuList);
    }
}
