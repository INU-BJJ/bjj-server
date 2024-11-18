package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.MyReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;

import java.util.List;
import java.util.Map;

public interface ReviewRepositoryCustom {

    Long countReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages);
    List<ReviewDetailRes> findReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages);
    Map<String, List<MyReviewDetailRes>> findMyReviewsWithImagesAndMemberDetailsAndCafeteria(Long memberId);
    Long countMyReviewsWithImagesAndMemberDetailsByCafeteria(Long memberId, String cafeteriaName);
    List<ReviewDetailRes> findMyReviewsWithImagesAndMemberDetailsByCafeteria(Long memberId, String cafeteriaName, int pageNumber, int pageSize);
}
