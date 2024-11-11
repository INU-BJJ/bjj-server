package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;

import java.util.List;

public interface ReviewRepositoryCustom {

    Long countReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages);
    List<ReviewDetailRes> findReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages);
}
