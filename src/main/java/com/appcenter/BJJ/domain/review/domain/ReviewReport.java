package com.appcenter.BJJ.domain.review.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report_db")
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;

    private Long reportedId;

    private Long reviewId;

    private String content;

    private LocalDateTime createAt;

    @Builder
    private ReviewReport(Long reporterId, Long reviewId, Long reportedId, String content, LocalDateTime createAt) {
        this.reporterId = reporterId;
        this.reviewId = reviewId;
        this.reportedId = reportedId;
        this.content = content;
        this.createAt = createAt;
    }

    public static ReviewReport create(Long reporterId, Long reportedId, Long reviewId, String content) {
        return ReviewReport.builder()
                .reporterId(reporterId)
                .reportedId(reportedId)
                .reviewId(reviewId)
                .content(content)
                .createAt(LocalDateTime.now())
                .build();
    }
}
