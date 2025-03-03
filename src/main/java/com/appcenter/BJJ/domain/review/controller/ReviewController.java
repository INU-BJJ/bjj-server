package com.appcenter.BJJ.domain.review.controller;

import com.appcenter.BJJ.domain.menu.service.MenuPairService;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.MyReviewsGroupedRes;
import com.appcenter.BJJ.domain.review.dto.MyReviewsPagedRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewImageRes;
import com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import com.appcenter.BJJ.domain.review.dto.ReviewRes;
import com.appcenter.BJJ.domain.review.service.ReviewLikeService;
import com.appcenter.BJJ.domain.review.service.ReviewService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
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
                    - pageNumber는 0부터 시작 (0, 1, 2, ...)\s
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
                     - responseDTO : MyReviewsGroupedRes""")
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

    @Operation(summary = "특정 식당에 회원이 작성한 리뷰 조회",
            description = """
                    - 특정 식당에 회원이 작성한 리뷰 조회\s
                    - pageSize만큼 리뷰 조회\s
                    - pageNumber은 1부터 시작 (0, 1, 2, ...)\s
                    - 마지막 페이지 여부 알려줌 (lastPage)\s
                    - responseDTO : MyReviewPagedRes""")
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

    @Operation(summary= "리뷰 이미지 조회", description = "메뉴쌍에 대한 리뷰 id와 리뷰 이미지 경로 목록 반환")
    @GetMapping("images")
    public ResponseEntity<ReviewImageRes> getImages(Long menuPairId, int pageNumber, int pageSize) {
        log.info("[로그] GET /api/reviews/images?menuPairId={}&pageNumber={}&pageSize={}", menuPairId, pageNumber, pageSize);

        ReviewImageRes reviewImageRes = reviewService.findReviewImagesByMenuPairId(menuPairId, pageNumber, pageSize);

        return ResponseEntity.ok(reviewImageRes);
    }

    @Operation(summary = "리뷰 상세 조회")
    @GetMapping("{reviewId}")
    public ResponseEntity<ReviewDetailRes> getReviewDetail(@PathVariable long reviewId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("[로그] GET /api/reviews/{}, memberNickname: {}", reviewId, userDetails.getNickname());

        ReviewDetailRes reviewDetailRes = reviewService.findReviewWithDetail(reviewId, userDetails.getMember().getId());

        return ResponseEntity.ok(reviewDetailRes);
    }
}
