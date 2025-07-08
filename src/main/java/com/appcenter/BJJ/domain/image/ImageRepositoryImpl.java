package com.appcenter.BJJ.domain.image;

import com.appcenter.BJJ.domain.review.domain.Review;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.appcenter.BJJ.domain.image.QImage.image;
import static com.appcenter.BJJ.domain.review.domain.QReview.review;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Image findFirstImageOfMostLikedReview(Long menuPairId) {

        // 1. 가장 좋아요가 많은 리뷰 조회
        Review mostLikedReview = queryFactory
                .selectFrom(review)
                .where(
                        review.menuPair().id.eq(menuPairId),
                        review.isDeleted.isFalse(),
                        review.images.size().gt(0)
                )
                .orderBy(review.likeCount.desc())
                .limit(1)
                .fetchOne();

        if (mostLikedReview == null) {
            return null;
        }

        // 2. 해당 리뷰에 연결된 이미지 1개 조회
        return queryFactory
                .selectFrom(image)
                .where(image.review().id.eq(mostLikedReview.getId()))
                .orderBy(image.id.asc())
                .limit(1)
                .fetchOne();
    }
}
