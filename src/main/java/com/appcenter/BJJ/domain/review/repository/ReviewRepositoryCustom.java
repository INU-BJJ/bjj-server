package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.BestReviewDto;
import com.appcenter.BJJ.domain.review.dto.BestReviewRes;
import com.appcenter.BJJ.domain.review.dto.MyReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;

public interface ReviewRepositoryCustom {

    Slice<ReviewDetailRes> findReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, Sort sort, Boolean isWithImages, Pageable pageable);
    Map<String, List<MyReviewDetailRes>> findMyReviewsWithImagesAndMemberDetailsAndCafeteria(Long memberId);
    Slice<MyReviewDetailRes> findMyReviewsWithImagesAndMemberDetailsByCafeteria(Long memberId, String cafeteriaName, Pageable pageable);
    List<BestReviewDto> findMostLikedReviewIdsInMainMenuIds(List<Long> mainMenuIds);
    BestReviewRes findBestReview(Long reviewId, Long memberId);
}
