package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMenuPairId(Long menuPairId);
}
