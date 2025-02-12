package com.appcenter.BJJ.domain.menu.repository;

import com.appcenter.BJJ.domain.menu.domain.Menu;
import com.appcenter.BJJ.domain.menu.dto.MenuInfoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findFirstByMenuNameAndCafeteriaId(String menuName, Long cafeteriaId);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.menu.dto.MenuInfoDto(
            m.id,
            m.menuName,
            c.name,
            c.corner
        )
        FROM Menu m
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE m.id IN :menuIds
    """)
    List<MenuInfoDto> findMenusWithCafeteriaInMenuIds(List<Long> menuIds);
}
