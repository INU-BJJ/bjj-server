package com.appcenter.BJJ.repository;

import com.appcenter.BJJ.domain.Review;
import com.appcenter.BJJ.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class ReviewSpecifications {

    // MainMenuId에 따라 리뷰를 필터링하는 Specification
    public static Specification<Review> withMainMenuId(Long mainMenuId) {
        return (root, query, criteriaBuilder) -> {
            if (mainMenuId == null) {
                return criteriaBuilder.conjunction(); // mainMenuId가 null이면 모든 리뷰 반환
            }
            // 주어진 mainMenuId와 일치하는 리뷰를 반환
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.join("menuPair").get("mainMenuId"), mainMenuId),
                    criteriaBuilder.equal(root.join("menuPair").get("subMenuId"), mainMenuId)
            );
        };
    }

    // SubMenuId에 따라 리뷰를 필터링하는 Specification
    public static Specification<Review> withSubMenuId(Long subMenuId) {
        return (root, query, criteriaBuilder) -> {
            if (subMenuId == null) {
                return criteriaBuilder.conjunction(); // subMenuId가 null이면 모든 리뷰 반환
            }
            // 주어진 subMenuId와 일치하는 리뷰를 반환
            return criteriaBuilder.or(
                    criteriaBuilder.equal(root.join("menuPair").get("mainMenuId"), subMenuId),
                    criteriaBuilder.equal(root.join("menuPair").get("subMenuId"), subMenuId)
            );
        };
    }

    // 리뷰에 이미지가 있는지 확인하는 Specification
    public static Specification<Review> withImages(Boolean isWithImages) {
        return (root, query, criteriaBuilder) -> {
            if (isWithImages != null && isWithImages) {
                // 이미지가 있는 리뷰만 반환
                return criteriaBuilder.greaterThan(criteriaBuilder.size(root.get("images")), 0);
            }
            return criteriaBuilder.conjunction(); // 항상 참을 반환 (필터링하지 않음)
        };
    }

    // 정렬 기준에 따라 리뷰를 정렬하는 Specification
    public static Specification<Review> sortedBy(Sort sort, Long mainMenuId, Long subMenuId) {
        return (root, query, criteriaBuilder) -> {
            if (sort == null) {
                return criteriaBuilder.conjunction(); // 정렬 기준이 null이면 모든 리뷰 반환
            }
            switch (sort) {
                case BEST_MATCH:
                    // BestMatch: mainMenu와 subMenu 둘 다 일치한 리뷰를 가장 먼저
                    Objects.requireNonNull(query).orderBy(
                            criteriaBuilder.asc(
                                    criteriaBuilder.selectCase()
                                            .when(criteriaBuilder.and(
                                                    criteriaBuilder.equal(root.join("menuPair").get("mainMenuId"), mainMenuId),
                                                    criteriaBuilder.equal(root.join("menuPair").get("subMenuId"), subMenuId)
                                            ), 1)
                                            .when(criteriaBuilder.equal(root.join("menuPair").get("mainMenuId"), mainMenuId), 2)
                                            .when(criteriaBuilder.equal(root.join("menuPair").get("subMenuId"), subMenuId), 3)
                                            .otherwise(4) // 일치하지 않는 경우
                            ),
                            criteriaBuilder.desc(root.get("id")) // 리뷰 ID 기준으로 내림차순 정렬
                    );
                    break;
                case NEWEST_FIRST:
                    // NewestFirst: reviewId 기준으로 최신순 정렬
                    Objects.requireNonNull(query).orderBy(criteriaBuilder.desc(root.get("id")));
                    break;
                case MOST_LIKED:
                    // MostLiked: 좋아요 수(likeCount) 기준으로 정렬
                    Objects.requireNonNull(query).orderBy(criteriaBuilder.desc(root.get("likeCount")), criteriaBuilder.desc(root.get("id"))); // 좋아요 수 기준 후 reviewId 기준으로 정렬
                    break;
                default:
                    // 지정되지 않은 경우 기본적으로 모든 리뷰 반환
                    break;
            }
            return criteriaBuilder.conjunction(); // 항상 참을 반환
        };
    }
}
