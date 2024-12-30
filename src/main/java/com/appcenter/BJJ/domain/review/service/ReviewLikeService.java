package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.domain.ReviewLike;
import com.appcenter.BJJ.domain.review.repository.ReviewLikeRepository;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public boolean toggleReviewLike(long reviewId, long memberId) {
        log.info("[로그] toggleReviewLike(), reviewId : {}, memberId: {}", reviewId, memberId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 자신의 리뷰에는 좋아요를 시도할 수 없음
        if (review.getMemberId() == memberId) {
            log.warn("회원 {}이 자신의 리뷰 {}에 좋아요를 시도했습니다.", memberId, reviewId);
            throw new CustomException(ErrorCode.CANNOT_LIKE_OWN_REVIEW);
        }

        // 현재 사용자가 해당 리뷰를 좋아요 했는지 확인
        boolean isLiked = reviewLikeRepository.existsByReviewIdAndMemberId(reviewId, memberId);

        if (isLiked) {
            // 좋아요 취소 처리
            reviewLikeRepository.deleteByReviewIdAndMemberId(reviewId, memberId);
            review.decrementLikeCount(); // 좋아요 개수 감소
        } else {
            // 좋아요 추가 처리
            ReviewLike reviewLike = ReviewLike.builder()
                    .reviewId(reviewId)
                    .memberId(memberId)
                    .build();
            reviewLikeRepository.save(reviewLike);

            review.incrementLikeCount(); // 좋아요 개수 증가
        }

        // 최종 상태 반환: true면 좋아요 추가, false면 좋아요 취소
        return !isLiked;
    }
}
