package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.menu.dto.MenuRatingStatsDto;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.dto.MemberCharacterImageDto;
import com.appcenter.BJJ.domain.review.dto.ReviewImageDetailRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    @Query("""
        SELECT COUNT(r.id) > 0
        FROM Review r
        JOIN r.menuPair mp
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE r.createdDate = CURRENT_DATE
            AND r.memberId = :memberId
            AND r.isDeleted = false
            AND c.corner = :cafeteriaCorner
    """)
    boolean existsTodayUndeletedMyReviewByCafeteriaCorner(Long memberId, String cafeteriaCorner);

    @Query("""
        SELECT COUNT(r.id) > 0
        FROM Review r
        JOIN r.menuPair mp
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE r.createdDate = CURRENT_DATE
            AND r.memberId = :memberId
            AND r.isDeleted = false
            AND c.corner != "조식"
            AND c.corner != "석식"
    """)
    boolean existsTodayUndeletedMyReviewExcludingBreakfastAndDinner(Long memberId);

    @Query("""
        SELECT COUNT(r.id) > 0
        FROM Review r
        JOIN r.menuPair mp
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE r.createdDate = CURRENT_DATE
            AND r.memberId = :memberId
            AND c.corner = :cafeteriaCorner
    """)
    boolean existsTodayMyReviewByCafeteriaCorner(Long memberId, String cafeteriaCorner);

    @Query("""
        SELECT COUNT(r.id) > 0
        FROM Review r
        JOIN r.menuPair mp
        JOIN Menu m ON mp.mainMenuId = m.id
        JOIN Cafeteria c ON m.cafeteriaId = c.id
        WHERE r.createdDate = CURRENT_DATE
            AND r.memberId = :memberId
            AND c.corner != "조식"
            AND c.corner != "석식"
    """)
    boolean existsTodayReviewExcludingBreakfastAndDinner(Long memberId);

    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.isDeleted = false")
    Optional<Review> findUndeletedReviewById(Long id);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuPair.id = :menuPairId AND r.isDeleted = false")
    Float calculateAverageRatingByMenuPairId(Long menuPairId);

    @Query("SELECT COUNT(r.id) FROM Review r WHERE r.menuPair.id = :menuPairId AND r.isDeleted = false")
    int countByMenuPair_Id(Long menuPairId);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.review.dto.ReviewImageDetailRes(
            i.review.id,
            i.name
        )
        FROM Image i
        JOIN i.review r
        WHERE r.isDeleted = false
            AND (
                 r.menuPair.mainMenuId = :mainMenuId
                 OR r.menuPair.mainMenuId = :subMenuId
                 OR r.menuPair.subMenuId = :mainMenuId
                 OR r.menuPair.subMenuId = :subMenuId
             )
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
        WHERE mp.mainMenuId IN :mainMenuIds AND (r.isDeleted = false OR r IS NULL)
        GROUP BY mp.mainMenuId
    """)
    List<MenuRatingStatsDto> calculateRatingStatsByMainMenuIds(List<Long> mainMenuIds);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.isDeleted = false")
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
            mm.id,
            mm.menuName,
            sm.id,
            sm.menuName,
            m.id,
            m.nickname,
            CASE
                WHEN m.id = :memberId
                THEN TRUE
                ELSE FALSE
            END
        )
        FROM Review r
        JOIN r.menuPair mp
        JOIN Menu mm ON mp.mainMenuId = mm.id
        LEFT JOIN Menu sm ON mp.subMenuId = sm.id
        LEFT JOIN Member m ON r.memberId = m.id
        WHERE r.id = :reviewId AND r.isDeleted = false
    """)
    Optional<ReviewDetailRes> findReviewWithMenuAndMemberDetails(Long reviewId, Long memberId);

    @Query("""
        SELECT r.id
        FROM Review r
        LEFT JOIN ReviewLike rl ON rl.reviewId = r.id
        WHERE r.isDeleted = false
            AND r.createdDate >= :fromDate
        GROUP BY r.id
        HAVING COUNT(rl) >= 1
        ORDER BY COUNT(rl) DESC, r.createdDate DESC, r.id DESC
    """)
    List<Long> findBestReviewIds(LocalDate fromDate, Pageable pageable);

    @Query("""
        SELECT new com.appcenter.BJJ.domain.review.dto.MemberCharacterImageDto(
            iv.memberId,
            it.imageName
        )
        FROM Inventory iv
        JOIN Item it ON it.itemIdx = iv.itemIdx AND it.itemType = iv.itemType AND it.itemType = 'CHARACTER'
        WHERE iv.memberId IN :memberIds AND iv.isWearing = true
    """)
    List<MemberCharacterImageDto> findMemberCharacterImages(List<Long> memberIds);
}
