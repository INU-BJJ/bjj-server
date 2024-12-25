package com.appcenter.BJJ.domain.menu.repository;

import com.appcenter.BJJ.domain.menu.domain.Menu;
import com.appcenter.BJJ.domain.menu.domain.MenuLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuLikeRepository extends JpaRepository<MenuLike, Long> {
    boolean existsByMenuIdAndMemberId(long menuId, long memberId);

    void deleteByMenuIdAndMemberId(long menuId, long memberId);

    @Query("SELECT m FROM Menu m JOIN MenuLike ml ON m.id = ml.menuId WHERE ml.memberId = :memberId")
    List<Menu> findLikedMenusByMemberId(long memberId);
}
