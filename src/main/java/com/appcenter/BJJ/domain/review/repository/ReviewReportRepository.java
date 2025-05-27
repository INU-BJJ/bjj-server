package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    @Query("SELECT COUNT(DISTINCT r.reporterId) FROM ReviewReport r WHERE r.reviewId = :reviewId")
    Long countReviewReportByReviewId(Long reviewId);

    void deleteReviewReportsByReviewId(Long reviewId);

    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);
}
