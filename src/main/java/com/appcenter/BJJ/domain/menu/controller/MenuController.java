package com.appcenter.BJJ.domain.menu.controller;

import com.appcenter.BJJ.domain.menu.dto.MenuRankingsPagedRes;
import com.appcenter.BJJ.domain.menu.dto.MenuRes;
import com.appcenter.BJJ.domain.menu.service.MenuLikeService;
import com.appcenter.BJJ.domain.menu.service.MenuRankingService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MenuRankingService menuRankingService;

    @PostMapping("/{menuId}/like")
    @Operation(summary = "메뉴 좋아요 토글", description = "좋아요 추가 시 true, 좋아요 취소 시 false 반환")
    public ResponseEntity<Boolean> toggleLike(@PathVariable long menuId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/menus/{}/like", menuId);

        boolean isLiked = menuLikeService.toggleMenuLike(menuId, userDetails.getMember().getId());

        return ResponseEntity.ok(isLiked);
    }

    @GetMapping("/liked")
    @Operation(summary = "회원이 좋아요 누른 메뉴 조회")
    public ResponseEntity<List<MenuRes>> getLikedMenus(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/menus/liked");

        List<MenuRes> likedMenuList = menuLikeService.getLikedMenus(userDetails.getMember().getId());

        return ResponseEntity.ok(likedMenuList);
    }

    @GetMapping("/ranking")
    @Operation(summary = "이번 학기 메뉴 랭킹 조회",
            description = """
                    - 이번 학기의 메뉴 랭킹 목록을 불러옴
                    - pageSize: 한 번에 조회할 데이터 개수
                    - pageNumber: 0부터 시작하는 정수 (0, 1, 2, ...)
                    - lastPage: 마지막 페이지 여부
                    """)
    public ResponseEntity<MenuRankingsPagedRes> getRanking(int pageNumber, int pageSize) {
        log.info("[로그] GET /api/menus/ranking?pageNumber={}&pageSize={}", pageNumber, pageSize);

        MenuRankingsPagedRes menuRankingsPagedRes = menuRankingService.getMenuRanking(pageNumber, pageSize);

        return ResponseEntity.ok(menuRankingsPagedRes);
    }

    @PostMapping("/test/ranking")
    @Operation(summary = "[test] 전체 메뉴의 랭킹 업데이트")
    public ResponseEntity<Void> updateRanking() {
        log.info("[로그] POST /api/menus/test/ranking");

        menuRankingService.updateAllMenuRankings();

        return ResponseEntity.ok(null);
    }
}
