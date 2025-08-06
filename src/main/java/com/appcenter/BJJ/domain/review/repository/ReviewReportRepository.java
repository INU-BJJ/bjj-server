package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    @Query("""
            SELECT COUNT(DISTINCT r.reporterId)
            FROM ReviewReport r
            WHERE r.reviewId = :reviewId
            AND r.isDeleted = false
            """)
    Long countReviewReportByReviewId(Long reviewId);

    @Query("""
            SELECT r FROM ReviewReport r
            WHERE r.reviewId = :reviewId
            AND r.isDeleted = false
            """)
    List<ReviewReport> findAllByReviewId(Long reviewId);

    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);
}
