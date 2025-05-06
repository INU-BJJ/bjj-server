package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    Long countReviewReportByReviewId(Long reviewId);

    void deleteReviewReportsByReviewId(Long reviewId);

    boolean existsByIdAndReporterId(Long id, Long reporterId);
}
