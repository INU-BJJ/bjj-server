package com.appcenter.BJJ.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long>, ImageRepositoryCustom{

    @Query("SELECT i FROM Image i JOIN FETCH i.review r WHERE i.review.id IN :reviewIdList")
    List<Image> findByReviewIdList(List<Long> reviewIdList);

    @Query(value = """
        SELECT m.menuId,
               (
                 SELECT i2.name
                 FROM review_tb r2
                 JOIN menu_pair_tb mp2 ON mp2.id = r2.menu_pair_id
                 JOIN image_tb i2      ON i2.review_id = r2.id
                 WHERE mp2.main_menu_id = m.menuId
                   AND r2.is_deleted = false
                 ORDER BY r2.like_count DESC, r2.created_date DESC, i2.id ASC  -- 베스트 리뷰의 첫 이미지
                 LIMIT 1
               ) AS imageName
        FROM (
          SELECT DISTINCT main_menu_id AS menuId
          FROM menu_pair_tb
          WHERE main_menu_id IN :mainMenuIds
        ) m;
    """, nativeQuery = true)
    List<ImageDto> findFirstImagesOfMostLikedReviewInMainMenuIds(List<Long> mainMenuIds);
}
