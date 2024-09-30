package com.appcenter.BJJ.controller;

import com.appcenter.BJJ.domain.Sort;
import com.appcenter.BJJ.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.dto.ReviewRes;
import com.appcenter.BJJ.service.MenuPairService;
import com.appcenter.BJJ.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;
    private final MenuPairService menuPairService;

    @Operation(summary = "리뷰 작성")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> postReview(@RequestPart ReviewPost reviewPost, @RequestPart(required = false) List<MultipartFile> files, Long memberId) {

        // 리뷰 생성
        long reviewId = reviewService.create(reviewPost, files, memberId);

        // 리뷰 별점 및 개수 반영
        menuPairService.refreshReviewCountAndRating(reviewPost.getMenuPairId());

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewId);
    }

    @Operation(summary = "리뷰 조회",
            description = """
                    - 식당별 리뷰와 리뷰에 대한 모든 정보 보여줌\s
                    - 10개씩 리뷰 조회\s
                    - pageNumber은 1부터 시작 (1..10 -> 11->20 -> 21..30)\s
                    - 마지막 페이지 여부 알려줌 (lastPage)""")
    @GetMapping
    public ResponseEntity<ReviewRes> getReviews(Long menuPairId, int pageNumber, int pageSize, Sort sort, boolean isWithImages) {
        // page는 1부터 시작
        ReviewRes reviewRes = reviewService.findByMenuPair(menuPairId, pageNumber, pageSize, sort, isWithImages);

        return ResponseEntity.ok(reviewRes);
    }

    @Operation(summary = "회원이 작성한 리뷰 조회",
            description = "- 회원이 작성한 리뷰 조회 \n - 10개씩 리뷰 조회 \n - pageNumber은 1부터 시작 (1..10 -> 11->20 -> 21..30)")
    @GetMapping("/my")
    public ResponseEntity<ReviewRes> getMyReviews(Long memberId, int pageNumber, int pageSize) {

        ReviewRes reviewRes = reviewService.findMyReviews(memberId, pageNumber, pageSize);

        return ResponseEntity.ok(reviewRes);
    }

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Long> deleteReview(@PathVariable Long reviewId) {

        Long menuPairId = reviewService.delete(reviewId);

        // 리뷰 별점 및 개수 반영
        if (menuPairId != null) {
            menuPairService.refreshReviewCountAndRating(menuPairId);
        }

        return ResponseEntity.noContent().build();
    }

}
