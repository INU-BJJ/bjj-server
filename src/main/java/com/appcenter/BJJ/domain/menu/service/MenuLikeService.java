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
    public long addLikeToMenu(long menuId, long memberId) {
        log.info("[로그] addLikeToMenu() 시작, menuId: {}, memberId: {}", menuId, memberId);

        // 메뉴가 존재하지 않을 경우 예외
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 이미 좋아요를 누른 경우 예외
        if (menuLikeRepository.existsByMenuIdAndMemberId(menuId, memberId)) {
            log.warn("회원 {}이 이미 좋아요를 누른 메뉴 {}에 다시 좋아요를 시도했습니다.", memberId, menuId);
            throw new CustomException(ErrorCode.ALREADY_LIKED_MENU);
        }

        // 메뉴의 좋아요 수 증가
        menu.incrementLikeCount();

        // 메뉴 좋아요 엔티티 생성 및 저장
        MenuLike menuLike = MenuLike.builder()
                .menuId(menuId)
                .memberId(memberId)
                .build();

        return menuLikeRepository.save(menuLike).getId();
    }

    @Transactional
    public void removeLikeFromMenu(long menuId, long memberId) {
        log.info("[로그] removeLikeFromMenu() 시작, menuId: {}, memberId: {}", menuId, memberId);

        // 메뉴가 존재하지 않을 경우 예외
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 이미 좋아요를 누른 경우 예외
        if (!menuLikeRepository.existsByMenuIdAndMemberId(menuId, memberId)) {
            log.warn("회원 {}이 좋아요를 누르지 않은 메뉴 {}에 좋아요 취소를 시도했습니다.", memberId, menuId);
            throw new CustomException(ErrorCode.NOT_LIKED_MENU);
        }

        // 메뉴의 좋아요 수 감소
        menu.decrementLikeCount();

        // 메뉴 좋아요 엔티티 제거
        menuLikeRepository.deleteByMenuIdAndMemberId(menuId, memberId);
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
