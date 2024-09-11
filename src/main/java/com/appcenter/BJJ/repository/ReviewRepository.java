package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMenuPairId(Long menuPairId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuPairId = :menuPairId")
    Float findAverageRatingByMenuPairId(Long menuPairId);
}
