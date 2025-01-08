package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.dto.ReviewImageDetailRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuPair.id = :menuPairId")
    Float findAverageRatingByMenuPairId(Long menuPairId);

    int countByMenuPair_Id(Long menuPairId);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.review.dto.ReviewImageDetailRes(
            i.review.id,
            i.name
        )
        FROM Image i
        WHERE i.review.menuPair.mainMenuId = :mainMenuId OR i.review.menuPair.mainMenuId = :subMenuId
            OR i.review.menuPair.subMenuId = :mainMenuId OR i.review.menuPair.subMenuId = :subMenuId
        ORDER BY i.id DESC
    """)
    Slice<ReviewImageDetailRes> findReviewImagesByMenuPairId(Long mainMenuId, Long subMenuId, Pageable pageable);
}
