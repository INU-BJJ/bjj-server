package com.appcenter.BJJ.domain.review.controller;

import com.appcenter.BJJ.domain.menu.service.MenuPairService;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.MyReviewsGroupedRes;
import com.appcenter.BJJ.domain.review.dto.MyReviewsPagedRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.review.dto.ReviewRes;
import com.appcenter.BJJ.domain.review.service.ReviewLikeService;
import com.appcenter.BJJ.domain.review.service.ReviewService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;
    private final MenuPairService menuPairService;
    private final ReviewLikeService reviewLikeService;

    @Operation(summary = "리뷰 작성")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> postReview(@RequestPart ReviewPost reviewPost, @RequestPart(required = false) List<MultipartFile> files, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/reviews, memberNickname : {}", userDetails.getNickname());

        // 리뷰 생성
        long reviewId = reviewService.create(reviewPost, files, userDetails.getMember().getId());

        // 리뷰 별점 및 개수 반영
        menuPairService.refreshReviewCountAndRating(reviewPost.getMenuPairId());

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewId);
    }

    @Operation(summary = "리뷰 조회",
            description = """
                    - 식당별 리뷰와 리뷰에 대한 모든 정보 보여줌\s
                    - pageSize만큼 리뷰 조회\s
                    - pageNumber는 0부터 시작 (0..9 -> 10->19 -> 20..29)\s
                    - 마지막 페이지 여부 알려줌 (lastPage)\s
                    - sort는 정렬 방법 (BestMatch : 메뉴일치순, MostLiked : 좋아요순, NewestFirst : 최신순)
                    - isWithImages는 포토리뷰만 여부
                    - responseDTO : ReviewRes
                    """)
    @GetMapping
    public ResponseEntity<ReviewRes> getReviews(Long menuPairId, int pageNumber, int pageSize, Sort sort, boolean isWithImages, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews?menuPairId={}&pageNumber={}&pageSize={}&sort={}&isWithImages={}", menuPairId, pageNumber, pageSize, sort, isWithImages);

        // page는 1부터 시작
        ReviewRes reviewRes = reviewService.findByMenuPair(userDetails.getMember().getId(), menuPairId, pageNumber, pageSize, sort, isWithImages);

        return ResponseEntity.ok(reviewRes);
    }

    @Operation(summary = "회원이 작성한 리뷰 조회",
            description = """
                    - 회원이 작성한 리뷰 조회\s
                     - 각 식당별 최대 3개씩 리뷰 조회\s
                     - responseDTO : MyReviewRes""")
    @GetMapping("/my")
    public ResponseEntity<MyReviewsGroupedRes> getMyReviews(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/my, memberNickname: {}", userDetails.getNickname());

        MyReviewsGroupedRes myReviewRes = reviewService.findMyReviews(userDetails.getMember().getId());

        return ResponseEntity.ok(myReviewRes);
    }

    @Operation(summary = "특정 식당에 회원이 작성한 리뷰 조회",
            description = """
                    - 특정 식당에 회원이 작성한 리뷰 조회\s
                    - pageSize만큼 리뷰 조회\s
                    - pageNumber은 1부터 시작 (0..9 -> 10->19 -> 20..29)\s
                    - 마지막 페이지 여부 알려줌 (lastPage)\s
                    - responseDTO : ReviewRes""")
    @GetMapping("/my/cafeteria")
    public ResponseEntity<MyReviewsPagedRes> getMyReviewsByCafeteria(String cafeteriaName, int pageNumber, int pageSize, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/my/cafeteria?cafeteriaName={}, memberNickname: {}", cafeteriaName, userDetails.getNickname());

        MyReviewsPagedRes myReviewsPagedRes = reviewService.findMyReviewsByCafeteria(userDetails.getMember().getId(), cafeteriaName, pageNumber, pageSize);

        return ResponseEntity.ok(myReviewsPagedRes);
    }

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰 삭제시 noContent")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Long> deleteReview(@PathVariable Long reviewId) {
        log.info("[로그] DELETE /api/reviews/{}", reviewId);

        Long menuPairId = reviewService.delete(reviewId);

        // 리뷰 별점 및 개수 반영
        if (menuPairId != null) {
            menuPairService.refreshReviewCountAndRating(menuPairId);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "리뷰 좋아요 토글", description = "좋아요 추가 시 true, 좋아요 취소 시 false 반환")
    @PostMapping("{reviewId}/like")
    public ResponseEntity<Boolean> toggleLike(@PathVariable long reviewId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] POST /api/reviews/{}/like, memberNickname: {}", reviewId, userDetails.getNickname());

        boolean isLiked = reviewLikeService.toggleReviewLike(reviewId, userDetails.getMember().getId());

        return ResponseEntity.ok(isLiked);
    }

    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("{reviewId}")
    public ResponseEntity<ReviewDetailRes> getReviewDetail(@PathVariable long reviewId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/{}, memberNickname: {}", reviewId, userDetails.getNickname());

        ReviewDetailRes reviewDetailRes = reviewService.findReviewWithDetail(reviewId, userDetails.getMember().getId());

        return ResponseEntity.ok(reviewDetailRes);
    }
}
