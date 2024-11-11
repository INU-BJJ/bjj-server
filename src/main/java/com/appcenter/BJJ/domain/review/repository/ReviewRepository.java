package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    Page<Review> findByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuPair.id = :menuPairId")
    Float findAverageRatingByMenuPairId(Long menuPairId);

    int countByMenuPair_Id(Long menuPairId);
}
