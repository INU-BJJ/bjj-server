package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByMemberIdAndReviewId(long memberId, long reviewId);

    void deleteByMemberIdAndReviewId(long memberId, long reviewId);
}
