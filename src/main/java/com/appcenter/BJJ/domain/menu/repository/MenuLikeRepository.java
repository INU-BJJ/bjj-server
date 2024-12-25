package com.appcenter.BJJ.domain.menu.repository;

import com.appcenter.BJJ.domain.menu.domain.MenuLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuLikeRepository extends JpaRepository<MenuLike, Long> {

    boolean existsByMenuIdAndMemberId(long menuId, long memberId);
}
