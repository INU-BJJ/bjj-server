package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.member.enums.MemberStatus;
import com.appcenter.BJJ.domain.member.schedule.MemberTaskRepository;
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
    private final MemberTaskRepository memberTaskRepository;
    public static final int REPORT_COUNT = 5;
    public static final int BAN_COUNT = 3;

    @Transactional
    public void reportReview(Long reporterId, Long reviewId, ReviewReportReq reviewReportReq) {
        Long reportedId = reviewRepository.findById(reviewId).orElseThrow( //리뷰 작성자
                () -> new CustomException(ErrorCode.REVIEW_NOT_FOUND)
        ).getMemberId();

        //회원의 신고 체크
        validateReportReview(reporterId, reportedId, reviewId);
        //작성자의 리뷰 체크
        handleReportedReview(reportedId, reviewId);

        reviewReportRepository.saveAll(reviewReportReq.getContent()
                .stream()
                .map(content ->
                        ReviewReport.create(reporterId, reportedId, reviewId, content))
                .toList());
    }

    private void validateReportReview(Long reporterId, Long reportedId, Long reviewId) {
        if (Objects.equals(reporterId, reportedId)) {
            throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_REVIEW);
        }

        if (reviewReportRepository.existsByReviewIdAndReporterId(reviewId, reporterId)) {
            throw new CustomException(ErrorCode.DUPLICATE_REPORT);
        }
    }

    private void handleReportedReview(Long reportedId, Long reviewId) {
        Member member = memberRepository.findById(reportedId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //회원 밴 체크 (COMPLETE인 MemberTask가 3개 이상이면 밴 대상)
        if(memberTaskRepository.countByMemberId(member.getId()) >= BAN_COUNT) {
            member.updateMemberStatus(MemberStatus.BAN);
            return;
        }

        //회원 정지 체크
        if (reviewReportRepository.countReviewReportByReviewId(reviewId) >= REPORT_COUNT) {
            LocalDateTime now = LocalDateTime.now();
            member.updateMemberStatus(MemberStatus.SUSPENDED);
            memberTaskService.addOrUpdateTask(member.getId(), now, now.plusDays(7));

            delete(reviewId); //리뷰 신고 내역 sofe delete
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
