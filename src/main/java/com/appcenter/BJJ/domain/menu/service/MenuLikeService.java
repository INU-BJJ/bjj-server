package com.appcenter.BJJ.domain.menu.service;

import com.appcenter.BJJ.domain.menu.domain.Menu;
import com.appcenter.BJJ.domain.menu.domain.MenuLike;
import com.appcenter.BJJ.domain.menu.dto.MenuRes;
import com.appcenter.BJJ.domain.menu.repository.MenuLikeRepository;
import com.appcenter.BJJ.domain.menu.repository.MenuRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuLikeService {

    private final MenuLikeRepository menuLikeRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public boolean toggleMenuLike(long menuId, long memberId) {
        log.info("[로그] toggleMenuLike() 시작, menuId: {}, memberId: {}", menuId, memberId);

        // 메뉴가 존재하지 않을 경우 예외
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 현재 사용자가 해당 메뉴를 좋아요 했는지 확인
        boolean isLiked = menuLikeRepository.existsByMenuIdAndMemberId(menuId, memberId);

        if (isLiked) {
            // 좋아요 취소 처리
            menuLikeRepository.deleteByMenuIdAndMemberId(menuId, memberId);
            menu.decrementLikeCount(); // 좋아요 개수 감소
        } else {
            // 좋아요 추가 처리
            MenuLike menuLike = MenuLike.builder()
                    .menuId(menuId)
                    .memberId(memberId)
                    .build();
            menuLikeRepository.save(menuLike);

            menu.incrementLikeCount(); // 좋아요 개수 증가
        }

        // 최종 상태 반환: true면 좋아요 추가, false면 좋아요 취소
        return !isLiked;
    }

    public List<MenuRes> getLikedMenus(long memberId) {
        log.info("[로그] getLikedMenus() 시작, memberId: {}", memberId);

        List<Menu> likedMenuList = menuLikeRepository.findLikedMenusByMemberId(memberId);

        return likedMenuList.stream()
                .map(menu -> MenuRes.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getMenuName())
                        .build()
                ).toList();
    }
}
