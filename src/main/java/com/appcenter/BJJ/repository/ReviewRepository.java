package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
        SELECT r
        FROM Review r
        JOIN Menu m ON r.menuPair.mainMenuId = m.id OR r.menuPair.subMenuId = m.id
        WHERE m.id = :mainMenuId OR m.id = :subMenuId
        
    """)
    Page<Review> findByMainMenuIdOrSubMenuId(Long mainMenuId, Long subMenuId, Pageable pageable);

    Page<Review> findByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuPair.id = :menuPairId")
    Float findAverageRatingByMenuPairId(Long menuPairId);
}
