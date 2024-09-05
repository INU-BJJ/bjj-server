package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
