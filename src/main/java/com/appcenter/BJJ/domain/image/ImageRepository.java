package com.appcenter.BJJ.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long>, ImageRepositoryCustom{

    @Query("SELECT i FROM Image i JOIN FETCH i.review r WHERE i.review.id IN :reviewIdList")
    List<Image> findByReviewIdList(List<Long> reviewIdList);

    @Query(value = """
        WITH numberedImage AS (
             SELECT
                 mp.main_menu_id as menuId,
                 i.name as imageName,
                 ROW_NUMBER() OVER (
                     PARTITION BY mp.main_menu_id
                     ORDER BY r.like_count DESC, r.id DESC, i.id
                 ) AS row_num
             FROM review_tb r
             LEFT JOIN image_tb i ON r.id = i.review_id
             JOIN menu_pair_tb mp ON r.menu_pair_id = mp.id
             WHERE mp.main_menu_id IN :mainMenuIds AND r.is_deleted = false
        )
        SELECT
            menuId,
            imageName
        FROM numberedImage
        WHERE row_num = 1
    """, nativeQuery = true)
    List<ImageDto> findFirstImagesOfMostLikedReviewInMainMenuIds(List<Long> mainMenuIds);

    @Query("SELECT i FROM Image i JOIN FETCH i.review r WHERE i.review.id = :reviewId")
    List<Image> findByReviewId(Long reviewId);
}
