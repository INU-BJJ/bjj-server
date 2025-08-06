package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.member.schedule.MemberTaskService;
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
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewReportService {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final MemberRepository memberRepository;
    private final MemberTaskService memberTaskService;
    public static final int REPORT_COUNT = 5;

    @Transactional
    public void reportReview(Long reporterId, Long reviewId, ReviewReportReq reviewReportReq) {
        Long reportedId = reviewRepository.findById(reviewId).orElseThrow( //리뷰 작성자
                () -> new CustomException(ErrorCode.REVIEW_NOT_FOUND)
        ).getMemberId();

        if (Objects.equals(reporterId, reportedId)) {
            throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_REVIEW);
        }

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
            memberTaskService.addOrUpdateTask(member.getId(), now, now.plusDays(7));

            delete(reviewId); //리뷰 신고 내역 삭제
            reviewService.delete(reviewId); // 누적 신고 당한 리뷰 삭제
        }
    }

    @Transactional
    public void delete(Long reviewId) {
        List<ReviewReport> reviewReports = reviewReportRepository.findAllByReviewId(reviewId);
        for (ReviewReport reviewReport : reviewReports) {
            reviewReport.delete();
        }
    }
}
