package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.TodayDiet;
import com.appcenter.BJJ.dto.TodayDietRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TodayDietRepository extends JpaRepository<TodayDiet, Long> {

    @Query("""
                SELECT new com.appcenter.BJJ.dto.TodayDietRes(
                    td.id,
                    td.price,
                    td.kcal,
                    td.date,
                    td.mainMenuId,
                    m.menuName,
                    td.subMenuId,
                    td.restMenu,
                    c.name,
                    c.corner
                )
                FROM TodayDiet td
                JOIN Menu m ON td.mainMenuId = m.id
                JOIN Cafeteria c ON m.cafeteriaId = c.id
                WHERE c.name = :cafeteriaName AND td.date = CURRENT_DATE
            """)
    List<TodayDietRes> findTodayDietByCafeteriaName(String cafeteriaName);
}
