package com.appcenter.BJJ.domain.menu.repository;

import com.appcenter.BJJ.domain.menu.domain.MenuRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRankingRepository extends JpaRepository<MenuRanking, Long> {
    @Query("SELECT mr FROM MenuRanking mr WHERE mr.semester = :semester AND mr.menuId IN :menuIds")
    List<MenuRanking> findBySemesterInMenuIds(Integer semester, List<Long> menuIds);
}
