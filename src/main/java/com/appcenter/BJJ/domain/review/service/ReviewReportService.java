package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.review.domain.ReviewReport;
import com.appcenter.BJJ.domain.review.dto.ReviewReportReq;
import com.appcenter.BJJ.domain.review.repository.ReviewReportRepository;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewReportService {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final MemberRepository memberRepository;
    public static final int REPORT_COUNT = 5;

    @Transactional
    public void reportReview(Long reporterId, Long reviewId, ReviewReportReq reviewReportReq) {

        Long reportedId = reviewRepository.findById(reviewId).orElseThrow( //리뷰 작성자
                () -> new CustomException(ErrorCode.REVIEW_NOT_FOUND)
        ).getMemberId();

        if (reviewReportRepository.existsByReviewIdAndReporterId(reviewId, reporterId)) {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }

        reviewReportRepository.saveAll(reviewReportReq.getContent()
                .stream()
                .map(content ->
                        ReviewReport.create(reporterId, reportedId, reviewId, content))
                .toList());

        if (reviewReportRepository.countReviewReportByReviewId(reviewId) >= REPORT_COUNT) { // 일정 개수의 신고가 넘으면 작성자 정지
            Member member = memberRepository.findById(reportedId).orElseThrow(
                    () -> new CustomException(ErrorCode.USER_NOT_FOUND));

            LocalDateTime now = LocalDateTime.now();
            member.updateMemberStatus(MemberStatus.SUSPENDED);
            member.suspend(now, now.plusDays(7));

            reviewReportRepository.deleteReviewReportsByReviewId(reviewId);
            reviewService.delete(reviewId);
        }
    }
}
