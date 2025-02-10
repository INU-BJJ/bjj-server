package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.menu.dto.MenuRatingStatsDto;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.dto.ReviewImageDetailRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

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

    @Query("""
        SELECT new com.appcenter.BJJ.domain.review.dto.ReviewDetailRes(
            r.id,
            r.comment,
            r.rating,
            r.likeCount,
            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM ReviewLike rl
                    WHERE rl.reviewId = :reviewId
                        AND rl.memberId = :memberId
                )
                THEN TRUE
                ELSE FALSE
            END,
            r.createdDate,
            mp.id,
            mm.menuName,
            sm.menuName,
            m.id,
            m.nickname
        )
        FROM Review r
        JOIN r.menuPair mp
        JOIN Menu mm ON mp.mainMenuId = mm.id
        JOIN Menu sm ON mp.subMenuId = sm.id
        JOIN Member m ON r.memberId = m.id
        WHERE r.id = :reviewId
    """)
    Optional<ReviewDetailRes> findReviewWithMenuAndMemberDetails(Long reviewId, Long memberId);
}
