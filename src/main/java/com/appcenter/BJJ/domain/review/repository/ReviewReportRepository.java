package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    Long countReviewReportById(Long id);

    void deleteReviewReportsById(Long id);

    boolean existsByIdAndReporterId(Long id, Long reporterId);
}
