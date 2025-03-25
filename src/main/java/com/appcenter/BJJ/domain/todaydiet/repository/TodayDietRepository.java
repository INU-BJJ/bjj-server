package com.appcenter.BJJ.domain.todaydiet.repository;

import com.appcenter.BJJ.domain.todaydiet.dto.TodayDietRes;
import com.appcenter.BJJ.domain.todaydiet.dto.TodayMenuRes;
import com.appcenter.BJJ.domain.todaydiet.domain.TodayDiet;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TodayDietRepository extends JpaRepository<TodayDiet, Long> {

    boolean existsByDate(LocalDate date);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.todaydiet.dto.TodayDietRes(
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
            mp.reviewAverageRating,
            CASE WHEN ml.memberId = :memberId THEN true ELSE false END
        )
        FROM TodayDiet td
        JOIN MenuPair mp ON td.menuPairId = mp.id
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        LEFT JOIN MenuLike ml ON ml.menuId = m.id AND ml.memberId = :memberId
        WHERE c.name = :cafeteriaName AND td.date = CURRENT_DATE
    """)
    List<TodayDietRes> findTodayDietsByCafeteriaName(String cafeteriaName, long memberId);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.todaydiet.dto.TodayMenuRes(
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

    @Query("""
        SELECT mp.mainMenuId
        FROM TodayDiet td
        JOIN MenuPair mp ON td.menuPairId = mp.id
        WHERE td.date = :date
    """)
    List<Long> findMainMenuIdsByDate(LocalDate date);

    @Query("""
        SELECT mp.mainMenuId
        FROM TodayDiet td
        JOIN MenuPair mp ON td.menuPairId = mp.id
    """)
    List<Long> findAllMainMenuIds();
}
