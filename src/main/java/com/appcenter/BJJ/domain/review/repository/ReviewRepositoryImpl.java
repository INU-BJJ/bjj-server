package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.menu.domain.Menu;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.domain.ReviewLike;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Long countReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Review> review = countQuery.from(Review.class);

        // 조건문 리스트
        List<Predicate> predicates = new ArrayList<>();

        // MenuPair 조인
        Join<Review, MenuPair> menuPair = review.join("menuPair", JoinType.INNER);

        // Menu 조인
        Root<Menu> mainMenu = countQuery.from(Menu.class);
        predicates.add(cb.equal(mainMenu.get("id"), menuPair.get("mainMenuId")));
        Root<Menu> subMenu = countQuery.from(Menu.class);
        predicates.add(cb.equal(subMenu.get("id"), menuPair.get("subMenuId")));

        // Menu 조건문
        predicates.add(cb.or(
                cb.equal(mainMenu.get("id"), mainMenuId),
                cb.equal(mainMenu.get("id"), subMenuId),
                cb.equal(subMenu.get("id"), mainMenuId),
                cb.equal(subMenu.get("id"), subMenuId)
        ));

        // 포토 리뷰만 조건 추가
        if (Boolean.TRUE.equals(isWithImages)) {
            predicates.add(cb.greaterThan(cb.size(review.get("images")), 0));
        }

        countQuery.select(cb.count(review)).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public List<ReviewDetailRes> findReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, int pageNumber, int pageSize, Sort sort, Boolean isWithImages) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ReviewDetailRes> query = cb.createQuery(ReviewDetailRes.class);
        Root<Review> review = query.from(Review.class);

        // 조건문 리스트
        List<Predicate> predicates = new ArrayList<>();

        // MenuPair와 Image 조인
        Join<Review, MenuPair> menuPair = review.join("menuPair", JoinType.INNER);

        // Menu 조인
        Root<Menu> mainMenu = query.from(Menu.class);
        predicates.add(cb.equal(mainMenu.get("id"), menuPair.get("mainMenuId")));
        Root<Menu> subMenu = query.from(Menu.class);
        predicates.add(cb.equal(subMenu.get("id"), menuPair.get("subMenuId")));

        // Menu 조건문
        predicates.add(cb.or(
                cb.equal(mainMenu.get("id"), mainMenuId),
                cb.equal(mainMenu.get("id"), subMenuId),
                cb.equal(subMenu.get("id"), mainMenuId),
                cb.equal(subMenu.get("id"), subMenuId)
        ));

        // Member에 대한 서브쿼리
        Subquery<String> memberSubquery = query.subquery(String.class);
        Root<Member> member = memberSubquery.from(Member.class);
        memberSubquery.select(member.get("nickname"))
                .where(cb.equal(member.get("id"), review.get("memberId")));

        // ReviewLike에 대한 서브쿼리
        Subquery<Long> likeSubquery = query.subquery(Long.class);
        Root<ReviewLike> reviewLike = likeSubquery.from(ReviewLike.class);
        likeSubquery.select(reviewLike.get("id"))
                .where(cb.and(
                        cb.equal(reviewLike.get("reviewId"), review.get("id")),
                        cb.equal(reviewLike.get("memberId"), memberId)
                ));
        Predicate isLiked = cb.exists(likeSubquery);

        // 포토 리뷰만 조건 추가
        if (Boolean.TRUE.equals(isWithImages)) {
            predicates.add(cb.greaterThan(cb.size(review.get("images")), 0));
        }

        // 정렬 조건 설정
        List<Order> orderList = new ArrayList<>();
        if (sort == Sort.BEST_MATCH) {
            // BestMatch: mainMenu와 subMenu 둘 다 일치한 리뷰를 가장 먼저
            Expression<Integer> bestMatchPriority = cb.<Integer>selectCase()
                    .when(cb.and(
                            cb.equal(menuPair.get("mainMenuId"), mainMenuId),
                            cb.equal(menuPair.get("subMenuId"), subMenuId)
                    ), 1)
                    .when(cb.equal(menuPair.get("mainMenuId"), mainMenuId), 2)
                    .when(cb.equal(menuPair.get("subMenuId"), subMenuId), 3)
                    .otherwise(4); // 일치하지 않는 경우

            orderList.add(cb.asc(bestMatchPriority)); // 우선순위가 낮을수록 먼저 정렬
            orderList.add(cb.desc(review.get("id"))); // 리뷰 ID 기준 내림차순 정렬
        } else if (sort == Sort.NEWEST_FIRST) {
            // NewestFirst: reviewId 기준으로 최신순 정렬
            orderList.add(cb.desc(review.get("id")));
        } else if (sort == Sort.MOST_LIKED) {
            // MostLiked: 좋아요 수(likeCount) 기준으로 정렬
            orderList.add(cb.desc(review.get("likeCount")));
            orderList.add(cb.desc(review.get("id"))); // 좋아요 수 동일 시 reviewId 기준으로 정렬
        }
        query.orderBy(orderList);

        // 쿼리에서 선택할 내용 설정
        query.select(cb.construct(
                ReviewDetailRes.class,
                review.get("id"),
                review.get("comment"),
                review.get("rating"),
                review.get("likeCount"),
                isLiked,
                review.get("createdDate"),
                menuPair.get("id"),
                mainMenu.get("menuName"),
                subMenu.get("menuName"),
                review.get("memberId"),
                memberSubquery
        )).where(predicates.toArray(new Predicate[0]));

        // 페이징 처리
        TypedQuery<ReviewDetailRes> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageNumber);
        typedQuery.setMaxResults(pageSize);

        return typedQuery.getResultList();
    }
}
