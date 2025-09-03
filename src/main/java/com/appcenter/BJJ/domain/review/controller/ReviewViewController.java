package com.appcenter.BJJ.domain.review.controller;

import com.appcenter.BJJ.domain.review.domain.Period;
import com.appcenter.BJJ.domain.review.dto.BestReviewRes;
import com.appcenter.BJJ.domain.review.service.ReviewService;
import com.appcenter.BJJ.global.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/view/reviews")
@RequiredArgsConstructor
public class ReviewViewController {

    private final ReviewService reviewService;

    @Operation(summary = "베스트 리뷰 페이지 조회",
            description = """
                - 좋아요를 가장 많이 받은 리뷰 출력
                - period=DAY: 오늘 하루, period=WEEK: 이번 주, period=MONTH: 이번 달, period=SEMESTER: 이번 학기 기준
                - 동일한 좋아요 수를 받은 리뷰가 있을 경우 최신 리뷰 우선
                - 최소 좋아요 개수 1개
                - 조건을 충족하는 리뷰가 없는 경우 '지금 당장 리뷰를 작성하고 ‘최고의 리뷰어’가 되어보세요!!' 문구 출력
                """)
    @GetMapping("/best")
    public String bestReviewPage(
            Model model,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "조회 기간 (DAY | WEEK | MONTH | SEMESTER)", example = "DAY")
            @RequestParam(defaultValue = "DAY") Period period
    ) {
        log.info("[로그] GET /view/reviews/best?period={}, memberNickname: {}", period, userDetails.getNickname());

        BestReviewRes bestReviewRes = reviewService.findBestReviewByPeriod(userDetails.getMember().getId(), period)
                .orElseGet(BestReviewRes::new);

        model.addAttribute("review", bestReviewRes);
        return "banners/best-review"; // templates/banners/best-review.html
    }
}
