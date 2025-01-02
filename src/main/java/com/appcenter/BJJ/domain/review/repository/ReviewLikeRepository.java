package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewIdAndMemberId(long reviewId, long memberId);

    void deleteByReviewIdAndMemberId(long reviewId, long memberId);
}
