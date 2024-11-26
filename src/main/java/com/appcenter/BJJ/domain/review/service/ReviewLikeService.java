package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.dto.ReviewLikeReq.ReviewLikeDelete;
import com.appcenter.BJJ.domain.review.dto.ReviewLikeReq.ReviewLikePost;
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
    public Long create(ReviewLikePost reviewLikePost, long memberId) {
        log.info("[로그] create(), reviewLikePost.getReviewId : {}", reviewLikePost.getReviewId());

        Review review = reviewRepository.findById(reviewLikePost.getReviewId())
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 자신의 리뷰에는 좋아요를 시도할 수 없음
        if (review.getMemberId() == memberId) {
            log.warn("회원 {}이 자신의 리뷰 {}에 좋아요를 시도했습니다.", memberId, review.getId());
            throw new CustomException(ErrorCode.CANNOT_LIKE_OWN_REVIEW);
        }

        // 이미 좋아요 누른 리뷰인 경우 좋아요를 할 수 없음
        if (reviewLikeRepository.existsByMemberIdAndReviewId(memberId, review.getId())) {
            log.warn("회원 {}이 이미 좋아요를 누른 리뷰 {}에 다시 좋아요를 시도했습니다.", memberId, review.getId());
            throw new CustomException(ErrorCode.ALREADY_LIKED_REVIEW);
        }

        // 리뷰 엔티티에서 좋아요 개수 필드 업데이트
        review.increaseLikeCount();

        return reviewLikeRepository.save(reviewLikePost.toEntity(memberId)).getId();
    }

    @Transactional
    public void delete(ReviewLikeDelete reviewLikeDelete, long memberId) {
        log.info("[로그] delete(), reviewLikeDelete.getReviewId : {}", reviewLikeDelete.getReviewId());

        Review review = reviewRepository.findById(reviewLikeDelete.getReviewId())
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        // 자신의 리뷰에는 좋아요 취소를 시도할 수 없음
        if (review.getMemberId() == memberId) {
            log.warn("회원 {}이 자신의 리뷰 {}에 좋아요 취소를 시도했습니다.", memberId, review.getId());
            throw new CustomException(ErrorCode.CANNOT_UNLIKE_OWN_REVIEW);
        }

        // 좋아요를 누르지 않은 리뷰인 경우 좋아요 취소를 할 수 없음
        if (!reviewLikeRepository.existsByMemberIdAndReviewId(memberId, review.getId())) {
            log.warn("회원 {}이 좋아요를 누르지 않은 리뷰 {}에 좋아요 취소를 시도했습니다.", memberId, review.getId());
            throw new CustomException(ErrorCode.NOT_LIKED_REVIEW);
        }

        // 리뷰 엔티티에서 좋아요 개수 필드 업데이트
        review.decreaseLikeCount();

        reviewLikeRepository.deleteByMemberIdAndReviewId(memberId, review.getId());
    }
}
