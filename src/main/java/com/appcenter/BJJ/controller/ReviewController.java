package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.dto.ReviewRes;
import com.appcenter.BJJ.service.MenuPairService;
import com.appcenter.BJJ.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MenuPairService menuPairService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> postReview(@RequestPart ReviewPost reviewPost, @RequestPart(required = false) List<MultipartFile> files, Long memberId) {

        // 리뷰 생성
        long reviewId = reviewService.create(reviewPost, files, memberId);

        // 리뷰 별점 반영
        menuPairService.reloadReviewAverageRating(reviewPost.getMenuPairId());

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewId);
    }

    @GetMapping
    public ResponseEntity<List<ReviewRes>> getReviewsByMenuPairId(Long menuPairId) {

        List<ReviewRes> reviewResList = reviewService.findByMenuPair(menuPairId);

        return ResponseEntity.ok(reviewResList);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Long> deleteReview(@PathVariable Long reviewId) {

        reviewService.delete(reviewId);

        return ResponseEntity.noContent().build();
    }

}
