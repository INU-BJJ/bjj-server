package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.todaydiet.domain.Cafeteria;
import com.appcenter.BJJ.domain.todaydiet.domain.CafeteriaData;
import com.appcenter.BJJ.domain.menu.domain.Menu;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.domain.ReviewLike;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.BestReviewDto;
import com.appcenter.BJJ.domain.review.dto.MyReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Slice<ReviewDetailRes> findReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, Sort sort, Boolean isWithImages, Pageable pageable) {

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

        // 자신이 작성한 리뷰인지 여부
        Expression<Object> isOwned = cb.selectCase()
                .when(cb.equal(review.get("memberId"), memberId), true)
                .otherwise(false);


        // 포토 리뷰만 조건 추가
        if (Boolean.TRUE.equals(isWithImages)) {
            predicates.add(cb.greaterThan(cb.size(review.get("images")), 0));
        }

        // 삭제되지 않은 리뷰인지 여부
        predicates.add(cb.equal(review.get("isDeleted"), false));

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
                mainMenu.get("id"),
                mainMenu.get("menuName"),
                subMenu.get("id"),
                subMenu.get("menuName"),
                review.get("memberId"),
                memberSubquery,
                isOwned
        )).where(predicates.toArray(new Predicate[0]));

        TypedQuery<ReviewDetailRes> typedQuery = entityManager.createQuery(query);

        // Slice 객체로 변환 후 반환
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        typedQuery.setFirstResult(pageNumber * pageSize);
        typedQuery.setMaxResults(pageSize + 1);

        List<ReviewDetailRes> resultList = typedQuery.getResultList();

        // 다음 페이지 존재 여부 판단
        boolean hasNext = resultList.size() > pageSize;

        // 결과를 한 페이지 크기로 제한
        if (hasNext) {
            resultList = resultList.subList(0, pageSize);
        }

        // Slice 객체로 반환
        return new SliceImpl<>(resultList, pageable, hasNext);
    }

    @Override
    public Map<String, List<MyReviewDetailRes>> findMyReviewsWithImagesAndMemberDetailsAndCafeteria(Long memberId) {

        List<String> cafeteriaNameList = Arrays.stream(CafeteriaData.values()).map(CafeteriaData::getName).toList();

        Map<String, List<MyReviewDetailRes>> myReviewDetailList = new LinkedHashMap<>();

        for (String cafeteriaName : cafeteriaNameList) {

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<MyReviewDetailRes> query = cb.createQuery(MyReviewDetailRes.class);
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

            // Cafeteria 조인
            Root<Cafeteria> cafeteria = query.from(Cafeteria.class);
            predicates.add(cb.equal(cafeteria.get("id"), mainMenu.get(("cafeteriaId"))));

            // Cafeteria 조건문
            predicates.add(cb.equal(cafeteria.get("name"), cafeteriaName));

            // Member 조건문
            predicates.add(cb.equal(review.get("memberId"), memberId));

            // Member에 대한 서브쿼리
            Subquery<String> memberSubquery = query.subquery(String.class);
            Root<Member> member = memberSubquery.from(Member.class);
            memberSubquery.select(member.get("nickname"))
                    .where(cb.equal(member.get("id"), review.get("memberId")));

            // 삭제되지 않은 리뷰인지 여부
            predicates.add(cb.equal(review.get("isDeleted"), false));

            // 쿼리에서 선택할 내용 설정
            query.select(cb.construct(
                    MyReviewDetailRes.class,
                    review.get("id"),
                    review.get("comment"),
                    review.get("rating"),
                    review.get("likeCount"),
                    review.get("createdDate"),
                    menuPair.get("id"),
                    mainMenu.get("menuName"),
                    subMenu.get("menuName"),
                    review.get("memberId"),
                    memberSubquery
            )).where(predicates.toArray(new Predicate[0]));

            query.orderBy(cb.desc(review.get("id")));

            List<MyReviewDetailRes> resultList = entityManager.createQuery(query)
                    .setMaxResults(3)
                    .getResultList();

            if (!resultList.isEmpty()) {
                myReviewDetailList.put(cafeteriaName, resultList);
            }
        }
        return myReviewDetailList;
    }

    @Override
    public Slice<MyReviewDetailRes> findMyReviewsWithImagesAndMemberDetailsByCafeteria(Long memberId, String cafeteriaName, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MyReviewDetailRes> query = cb.createQuery(MyReviewDetailRes.class);
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

        // Cafeteria 조인
        Root<Cafeteria> cafeteria = query.from(Cafeteria.class);
        predicates.add(cb.equal(cafeteria.get("id"), mainMenu.get(("cafeteriaId"))));

        // Cafeteria 조건문
        predicates.add(cb.equal(cafeteria.get("name"), cafeteriaName));

        // Member 조건문
        predicates.add(cb.equal(review.get("memberId"), memberId));

        // Member에 대한 서브쿼리
        Subquery<String> memberSubquery = query.subquery(String.class);
        Root<Member> member = memberSubquery.from(Member.class);
        memberSubquery.select(member.get("nickname"))
                .where(cb.equal(member.get("id"), review.get("memberId")));

        // 삭제되지 않은 리뷰인지 여부
        predicates.add(cb.equal(review.get("isDeleted"), false));

        // 쿼리에서 선택할 내용 설정
        query.select(cb.construct(
                MyReviewDetailRes.class,
                review.get("id"),
                review.get("comment"),
                review.get("rating"),
                review.get("likeCount"),
                review.get("createdDate"),
                menuPair.get("id"),
                mainMenu.get("menuName"),
                subMenu.get("menuName"),
                review.get("memberId"),
                memberSubquery
        )).where(predicates.toArray(new Predicate[0]));

        query.orderBy(cb.desc(review.get("id")));


        TypedQuery<MyReviewDetailRes> typedQuery = entityManager.createQuery(query);

        // Slice 객체로 변환 후 반환
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        typedQuery.setFirstResult(pageNumber * pageSize);
        typedQuery.setMaxResults(pageSize + 1);

        List<MyReviewDetailRes> resultList = typedQuery.getResultList();

        // 다음 페이지 존재 여부 판단
        boolean hasNext = resultList.size() > pageSize;

        // 결과를 한 페이지 크기로 제한
        if (hasNext) {
            resultList = resultList.subList(0, pageSize);
        }

        // Slice 객체로 반환
        return new SliceImpl<>(resultList, pageable, hasNext);
    }

    @Override
    public List<BestReviewDto> findMostLikedReviewIdsInMainMenuIds(List<Long> mainMenuIds) {
        // EntityManager 생성
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BestReviewDto> query = cb.createQuery(BestReviewDto.class);

        // 조건문 리스트
        List<Predicate> predicates = new ArrayList<>();

        // Root 정의
        Root<Review> review = query.from(Review.class);
        Join<Review, MenuPair> menuPair = review.join("menuPair");

        // 서브쿼리 1: 최대 like_count를 구하는 서브쿼리
        Subquery<Long> subQuery1 = query.subquery(Long.class);
        Root<Review> subReview = subQuery1.from(Review.class);
        Join<Review, MenuPair> subMenuPair = subReview.join("menuPair");
        subQuery1.select(cb.max(subReview.get("likeCount")))
                .where(
                        cb.equal(subMenuPair.get("mainMenuId"), menuPair.get("mainMenuId")),
                        cb.equal(review.get("isDeleted"), false)    // 삭제되지 않은 리뷰인지 여부
                );

        // 서브쿼리 2: 최대 like_count와 같은 like_count 중에서 최대 review_id(가장 최근 리뷰)를 구하는 서브쿼리
        Subquery<Long> subQuery2 = query.subquery(Long.class);
        Root<Review> subReview2 = subQuery2.from(Review.class);
        Join<Review, MenuPair> subMenuPair2 = subReview2.join("menuPair");
        subQuery2.select(cb.max(subReview2.get("id")))
                .where(
                        cb.equal(subMenuPair2.get("mainMenuId"), menuPair.get("mainMenuId")),
                        cb.equal(subReview2.get("likeCount"), subQuery1),
                        cb.equal(review.get("isDeleted"), false)    // 삭제되지 않은 리뷰인지 여부
                );

        // 조건문 추가
        predicates.add(cb.equal(review.get("isDeleted"), false));   // 삭제되지 않은 리뷰인지 여부
        predicates.add(menuPair.get("mainMenuId").in(mainMenuIds));
        predicates.add(cb.equal(review.get("id"), subQuery2));  // review_id가 서브쿼리에서 구한 값과 일치하는 경우

        // 메인 쿼리 정의
        query.select(cb.construct(
                BestReviewDto.class,
                menuPair.get("mainMenuId"), // main_menu_id
                review.get("id") // review_id
                ))
                .where(predicates.toArray(new Predicate[0]));

        // 쿼리 실행
        return entityManager.createQuery(query).getResultList();
    }
}
