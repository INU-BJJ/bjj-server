package com.appcenter.BJJ.domain.image;

public interface ImageRepositoryCustom {

    Image findFirstImageOfMostLikedReview(Long menuPairId);
}
