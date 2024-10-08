package com.appcenter.BJJ.domain.image;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("""
                SELECT i
                FROM Image i
                WHERE i.review.id = (
                    SELECT r.id
                    FROM Review r
                    WHERE r.menuPair.id = :menuPairId AND SIZE(r.images) > 0
                    ORDER BY r.likeCount DESC
                )
                ORDER BY i.id ASC
            """)
    Image findFirstImageOfMostLikedReview(Long menuPairId, Limit limit);
}
