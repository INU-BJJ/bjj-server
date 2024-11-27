package com.appcenter.BJJ.domain.review.controller;

import com.appcenter.BJJ.domain.review.dto.ReviewLikeReq.ReviewLikeDelete;
import com.appcenter.BJJ.domain.review.dto.ReviewLikeReq.ReviewLikePost;
import com.appcenter.BJJ.domain.review.service.ReviewLikeService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/review-likes")
@RequiredArgsConstructor
@Tag(name = "ReviewLike", description = "리뷰 좋아요 API")
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @Operation(summary = "리뷰 좋아요")
    @PostMapping
    public ResponseEntity<Long> postReviewLike(@RequestBody ReviewLikePost reviewLikePost, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/review-likes, memberNickname: {}", userDetails.getNickname());

        Long reviewLikeId = reviewLikeService.create(reviewLikePost, userDetails.getMember().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewLikeId);
    }

    @Operation(summary = "리뷰 좋아요 취소")
    @DeleteMapping
    public ResponseEntity<Void> deleteReviewLike(@RequestBody ReviewLikeDelete reviewLikeDelete, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/review-likes, memberNickname: {}", userDetails.getNickname());

        reviewLikeService.delete(reviewLikeDelete, userDetails.getMember().getId());

        return ResponseEntity.noContent().build();
    }
}
