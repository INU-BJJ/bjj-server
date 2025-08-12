package com.appcenter.BJJ.domain.review.controller;

import com.appcenter.BJJ.domain.menu.service.MenuPairService;
import com.appcenter.BJJ.domain.review.domain.Period;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.*;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.review.service.ReviewLikeService;
import com.appcenter.BJJ.domain.review.service.ReviewReportService;
import com.appcenter.BJJ.domain.review.service.ReviewService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final ReviewReportService reviewReportService;

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

    @Operation(summary = "특정 식단의 리뷰 목록 조회",
            description = """
                    - 특정 식단(메뉴쌍)에 대한 모든 리뷰와 리뷰에 대한 정보를 불러옴
                    - pageSize: 한 번에 조회할 데이터 개수
                    - pageNumber: 0부터 시작하는 정수 (0, 1, 2, ...)
                    - lastPage: 마지막 페이지 여부
                    - sort: 정렬 방법 (BestMatch: 메뉴일치순, MostLiked: 좋아요순, NewestFirst: 최신순)
                    - isWithImages: 포토 리뷰만 조회할 지 여부 (true: 포토 리뷰만, false: 포토 리뷰 포함 모든 리뷰)
                    """)
    @GetMapping
    public ResponseEntity<ReviewsPagedRes> getReviews(Long menuPairId, int pageNumber, int pageSize, Sort sort, boolean isWithImages, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews?menuPairId={}&pageNumber={}&pageSize={}&sort={}&isWithImages={}", menuPairId, pageNumber, pageSize, sort, isWithImages);

        // page는 1부터 시작
        ReviewsPagedRes reviewsPagedRes = reviewService.findByMenuPair(userDetails.getMember().getId(), menuPairId, pageNumber, pageSize, sort, isWithImages);

        return ResponseEntity.ok(reviewsPagedRes);
    }

    @Operation(summary = "회원이 작성한 식당별 리뷰 목록 조회",
            description = """
                    - 회원이 작성한 모든 리뷰를 식당별로 분류하여 불러옴
                    - 각 식당별 최대 3개씩만 리뷰 조회
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "식당별 그룹된 리뷰",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MyReviewsGroupedRes.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "학생식당": [{"reviewId": 109, "comment": "맛이 차암 좋읍니다.", "rating": 5, "imageNames": ["7ff63a3b-a4a2-4142-b9f7-828a1e8c004d.jpg", "0992fb7e-0ec8-4804-8967-38843b808d09.png"], "likeCount": 0, "createdDate": "2025-02-03", "menuPairId": 81, "mainMenuName": "매콤순대볶음", "subMenuName": "호박새우젓국/미니돈까스*데미S", "memberId": 2, "memberNickname": "이춘삼", "memberImageName": null}],
                                      "2호관식당": [{"reviewId": 88, "comment": "맛이 차암 좋읍니다.", "rating": 5, "imageNames": ["53af5e4b-2f09-4803-a5e6-30d770e2171c.jpg", "20f6721e-db67-456c-95a3-1ad850601aa9.png"], "likeCount": 0, "createdDate": "2025-02-03", "menuPairId": 85, "mainMenuName": "순살닭볶음탕", "subMenuName": "계란파국", "memberId": 2, "memberNickname": "이춘삼", "memberImageName": null}]
                                    }""")))
    })
    @GetMapping("/my")
    public ResponseEntity<MyReviewsGroupedRes> getMyReviews(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/my, memberNickname: {}", userDetails.getNickname());

        MyReviewsGroupedRes myReviewRes = reviewService.findMyReviews(userDetails.getMember().getId());

        return ResponseEntity.ok(myReviewRes);
    }

    @Operation(summary = "특정 식당에 회원이 작성한 리뷰 목록 조회",
            description = """
                    - 특정 식당에 회원이 작성한 모든 리뷰를 불러옴
                    - pageSize: 한 번에 조회할 데이터 개수
                    - pageNumber: 0부터 시작하는 정수 (0, 1, 2, ...)
                    - lastPage: 마지막 페이지 여부
                    """)
    @GetMapping("/my/cafeteria")
    public ResponseEntity<MyReviewsPagedRes> getMyReviewsByCafeteria(String cafeteriaName, int pageNumber, int pageSize, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/my/cafeteria?cafeteriaName={}, memberNickname: {}", cafeteriaName, userDetails.getNickname());

        MyReviewsPagedRes myReviewsPagedRes = reviewService.findMyReviewsByCafeteria(userDetails.getMember().getId(), cafeteriaName, pageNumber, pageSize);

        return ResponseEntity.ok(myReviewsPagedRes);
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰 삭제 성공 시 응답으로 noContent")
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

    @Operation(summary= "특정 식단의 리뷰 이미지 목록 조회",
            description = """
                    - 특정 식단(메뉴쌍)에 대한 리뷰 id와 리뷰 사진 파일명 목록 반환
                    - pageSize: 한 번에 조회할 데이터 개수
                    - pageNumber: 0부터 시작하는 정수 (0, 1, 2, ...)
                    - lastPage: 마지막 페이지 여부
                    """)
    @GetMapping("images")
    public ResponseEntity<ReviewImagesPagedRes> getImages(Long menuPairId, int pageNumber, int pageSize) {
        log.info("[로그] GET /api/reviews/images?menuPairId={}&pageNumber={}&pageSize={}", menuPairId, pageNumber, pageSize);

        ReviewImagesPagedRes reviewImagesPagedRes = reviewService.findReviewImagesByMenuPairId(menuPairId, pageNumber, pageSize);

        return ResponseEntity.ok(reviewImagesPagedRes);
    }

    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("{reviewId}")
    public ResponseEntity<ReviewDetailRes> getReviewDetail(@PathVariable long reviewId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/{}, memberNickname: {}", reviewId, userDetails.getNickname());

        ReviewDetailRes reviewDetailRes = reviewService.findReviewWithDetail(reviewId, userDetails.getMember().getId());

        return ResponseEntity.ok(reviewDetailRes);
    }

    @Operation(summary = "리뷰 신고")
    @PostMapping("{reviewId}/report")
    public ResponseEntity<Boolean> reportReview(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long reviewId, @RequestBody ReviewReportReq reviewReportReq){
        reviewReportService.reportReview(userDetails.getMember().getId(), reviewId, reviewReportReq);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "베스트 리뷰 조회",
            description = """
                - 좋아요를 가장 많이 받은 리뷰 조회
                - period=DAY: 오늘 하루, period=WEEK: 이번 주, period=MONTH: 이번 달, period=SEMESTER: 이번 학기 기준
                - 동일한 좋아요 수를 받은 리뷰가 있을 경우 최신 리뷰 우선
                - 최소 좋아요 개수 1개
                - 조건을 충족하는 리뷰가 없는 경우 '204 No Content' 반환
                """)
    @GetMapping("/best")
    public ResponseEntity<BestReviewRes> getBestReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "조회 기간 (DAY | WEEK | MONTH | SEMESTER)", example = "WEEK")
            @RequestParam(defaultValue = "WEEK") Period period
    ) {
        log.info("[로그] GET /api/reviews/best?period={}, memberNickname: {}", period, userDetails.getNickname());

        return reviewService.findBestReviewByPeriod(userDetails.getMember().getId(), period)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
