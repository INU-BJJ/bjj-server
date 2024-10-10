package com.appcenter.BJJ.domain.menu.repository;

import com.appcenter.BJJ.domain.menu.dto.TodayDietRes;
import com.appcenter.BJJ.domain.menu.dto.TodayMenuRes;
import com.appcenter.BJJ.domain.menu.domain.TodayDiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TodayDietRepository extends JpaRepository<TodayDiet, Long> {

    boolean existsByDate(LocalDate date);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.menu.dto.TodayDietRes(
            td.id,
            td.price,
            td.kcal,
            td.date,
            td.menuPairId,
            mp.mainMenuId,
            m.menuName,
            mp.subMenuId,
            td.restMenu,
            c.name,
            c.corner,
            mp.reviewCount,
            mp.reviewAverageRating
        )
        FROM TodayDiet td
        JOIN MenuPair mp ON td.menuPairId = mp.id
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE c.name = :cafeteriaName AND td.date = CURRENT_DATE
    """)
    List<TodayDietRes> findTodayDietsByCafeteriaName(String cafeteriaName);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.menu.dto.TodayMenuRes(
            mp.id,
            m.menuName,
            c.id,
            c.name,
            c.corner
        )
        FROM TodayDiet td
        JOIN MenuPair mp ON td.menuPairId = mp.id
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE c.name = :cafeteriaName AND td.date = CURRENT_DATE
    """)
    List<TodayMenuRes> findTodayMainMenusByCafeteriaName(String cafeteriaName);
}