package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.domain.Image;
import com.appcenter.BJJ.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.dto.ReviewRes;
import com.appcenter.BJJ.service.ImageService;
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
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> postReview(@RequestPart ReviewPost reviewPost, @RequestPart(required = false) List<MultipartFile> files, Long memberId) {
        // .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.multipart.MaxUploadSizeExceededException: Maximum upload size exceeded]
        // 파일 최대 용량 초과 에러에 대한 예외 처리 필요
        List<Image> images = imageService.transformToReviewImage(files);
        long reviewId = reviewService.create(reviewPost, images, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewId);
    }

    @GetMapping
    public ResponseEntity<List<ReviewRes>> getReviewsByMenuPairId(Long menuPairId) {

        List<ReviewRes> reviewResList = reviewService.findByMenuPair(menuPairId);

        return ResponseEntity.ok(reviewResList);
    }


}
