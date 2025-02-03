package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.menu.dto.MenuRatingStatsDto;
import com.appcenter.BJJ.domain.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuPair.id = :menuPairId")
    Float findAverageRatingByMenuPairId(Long menuPairId);

    int countByMenuPair_Id(Long menuPairId);

    @Query("""        
        SELECT new com.appcenter.BJJ.domain.menu.dto.MenuRatingStatsDto(
            mp.mainMenuId,
            CAST(COALESCE(SUM(r.rating), 0) AS Long),
            CAST(COALESCE(COUNT(r.rating), 0) AS Long)
        )
        FROM Review r
        RIGHT JOIN r.menuPair mp
        WHERE mp.mainMenuId IN :mainMenuIds
        GROUP BY mp.mainMenuId
    """)
    List<MenuRatingStatsDto> calculateRatingStatsByMainMenuIds(List<Long> mainMenuIds);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    Float findAverageRating();
}
