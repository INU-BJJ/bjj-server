package com.appcenter.BJJ.domain.image;

import com.appcenter.BJJ.domain.review.domain.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImageRepositoryImpl implements ImageRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Image findFirstImageOfMostLikedReview(Long menuPairId) {
        // 1. 가장 좋아요 수가 많은 리뷰 조회
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> reviewQuery = cb.createQuery(Review.class);
        Root<Review> reviewRoot = reviewQuery.from(Review.class);

        reviewQuery.select(reviewRoot)
                .where(cb.and(
                        cb.equal(reviewRoot.get("menuPair").get("id"), menuPairId),
                        cb.gt(cb.size(reviewRoot.get("images")), 0)
                ))
                .orderBy(cb.desc(reviewRoot.get("likeCount")));

        // 2. 조회된 리뷰의 ID를 사용해 이미지 조회
        List<Review> reviews = entityManager.createQuery(reviewQuery)
                .setMaxResults(1) // 최상위 1개의 리뷰만 가져옴
                .getResultList();

        if (reviews.isEmpty()) {
            return null;
        }

        Long reviewId = reviews.get(0).getId();

        // 3. 해당 리뷰에 연결된 이미지를 가져옴
        CriteriaQuery<Image> imageQuery = cb.createQuery(Image.class);
        Root<Image> imageRoot = imageQuery.from(Image.class);

        imageQuery.select(imageRoot)
                .where(cb.equal(imageRoot.get("review").get("id"), reviewId))
                .orderBy(cb.asc(imageRoot.get("id")));

        return entityManager.createQuery(imageQuery)
                .setMaxResults(1) // 이미지도 최상위 1개만 가져옴
                .getSingleResult(); // 결과가 없을 시 NoResultException
    }
}
