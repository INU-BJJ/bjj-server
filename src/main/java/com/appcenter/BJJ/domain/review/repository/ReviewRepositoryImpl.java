package com.appcenter.BJJ.domain.review.repository;

import com.appcenter.BJJ.domain.menu.domain.QMenu;
import com.appcenter.BJJ.domain.review.domain.Sort;
import com.appcenter.BJJ.domain.review.dto.BestReviewDto;
import com.appcenter.BJJ.domain.review.dto.MyReviewDetailRes;
import com.appcenter.BJJ.domain.review.dto.ReviewDetailRes;
import com.appcenter.BJJ.domain.todaydiet.domain.CafeteriaData;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLSubQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.appcenter.BJJ.domain.member.domain.QMember.member;
import static com.appcenter.BJJ.domain.menu.domain.QMenuPair.menuPair;
import static com.appcenter.BJJ.domain.review.domain.QReview.review;
import static com.appcenter.BJJ.domain.review.domain.QReviewLike.reviewLike;
import static com.appcenter.BJJ.domain.todaydiet.domain.QCafeteria.cafeteria;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<ReviewDetailRes> findReviewsWithImagesAndMemberDetails(Long memberId, Long mainMenuId, Long subMenuId, Sort sort, Boolean isWithImages, Pageable pageable) {
        QMenu mainMenu = new QMenu("mainMenu");
        QMenu subMenu = new QMenu("subMenu");

        // 서브쿼리: 해당 리뷰에 좋아요를 눌렀는지 여부 (Boolean)
        BooleanExpression isLiked = JPAExpressions
                .selectOne()
                .from(reviewLike)
                .where(reviewLike.reviewId.eq(review.id)
                        .and(reviewLike.memberId.eq(memberId)))
                .exists();

        // where 조건 빌드
        BooleanBuilder whereBuilder = new BooleanBuilder();
        whereBuilder.and(review.isDeleted.isFalse());

        if (mainMenuId != null && subMenuId != null) {
            BooleanBuilder menuCondition = new BooleanBuilder();
            menuCondition.or(mainMenu.id.in(mainMenuId, subMenuId));
            menuCondition.or(subMenu.id.in(mainMenuId, subMenuId));
            whereBuilder.and(menuCondition);
        } else if (mainMenuId != null) {
            // subMenuId가 null인 경우
            whereBuilder.and(mainMenu.id.eq(mainMenuId));
        }

        if (Boolean.TRUE.equals(isWithImages)) {
            whereBuilder.and(review.images.size().gt(0));
        }

        // 기본 쿼리 생성
        JPQLQuery<ReviewDetailRes> query = jpaQueryFactory
                .select(Projections.constructor(
                        ReviewDetailRes.class,
                        review.id,
                        review.comment,
                        review.rating,
                        review.likeCount,
                        isLiked,
                        review.createdDate,
                        menuPair.id,
                        menuPair.mainMenuId,
                        mainMenu.menuName,
                        menuPair.subMenuId,
                        subMenu.menuName,
                        review.memberId,
                        member.nickname,
                        review.memberId.eq(memberId)
                ))
                .from(review)
                .join(review.menuPair(), menuPair)
                .join(mainMenu).on(menuPair.mainMenuId.eq(mainMenu.id))
                .leftJoin(subMenu).on(menuPair.subMenuId.eq(subMenu.id))
                .join(member).on(member.id.eq(review.memberId))
                .where(whereBuilder);

        // 정렬 조건 처리
        if (sort == Sort.BEST_MATCH && mainMenuId != null) {
            NumberExpression<Integer> bestMatchPriority;

            if (subMenuId != null) {
                bestMatchPriority = new CaseBuilder()
                        .when(menuPair.mainMenuId.eq(mainMenuId).and(menuPair.subMenuId.eq(subMenuId))).then(1) // 메인 서브 모두 일치
                        .when(menuPair.mainMenuId.eq(mainMenuId)).then(2)   // 메인만 일치
                        .when(menuPair.mainMenuId.eq(subMenuId).and(menuPair.subMenuId.eq(mainMenuId))).then(3) // 메인 서브 교차 일치
                        .otherwise(4);
            } else {
                bestMatchPriority = new CaseBuilder()
                        .when(menuPair.mainMenuId.eq(mainMenuId)).then(1)
                        .otherwise(2);
            }

            query.orderBy(bestMatchPriority.asc(), review.id.desc());
        } else if (sort == Sort.NEWEST_FIRST) {
            query.orderBy(review.id.desc());
        } else if (sort == Sort.MOST_LIKED) {
            query.orderBy(review.likeCount.desc(), review.id.desc());
        }

        // 페이징 처리
        List<ReviewDetailRes> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results = results.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    @Override
    public Map<String, List<MyReviewDetailRes>> findMyReviewsWithImagesAndMemberDetailsAndCafeteria(Long memberId) {
        QMenu mainMenu = new QMenu("mainMenu");
        QMenu subMenu = new QMenu("subMenu");

        // 카페테리아 이름 리스트
        List<String> cafeteriaNameList = Arrays.stream(CafeteriaData.values()).map(CafeteriaData::getName).toList();

        Map<String, List<MyReviewDetailRes>> myReviewDetailList = new LinkedHashMap<>();

        for (String cafeteriaName : cafeteriaNameList) {
            // 메인 쿼리 작성
            List<MyReviewDetailRes> results = jpaQueryFactory
                    .select(Projections.constructor(
                            MyReviewDetailRes.class,
                            review.id,
                            review.comment,
                            review.rating,
                            review.likeCount,
                            review.createdDate,
                            menuPair.id,
                            mainMenu.menuName,
                            subMenu.menuName,
                            review.memberId,
                            member.nickname
                    ))
                    .from(review)
                    .join(review.menuPair(), menuPair)
                    .join(mainMenu).on(menuPair.mainMenuId.eq(mainMenu.id))
                    .leftJoin(subMenu).on(menuPair.subMenuId.eq(subMenu.id))
                    .join(cafeteria).on(mainMenu.cafeteriaId.eq(cafeteria.id))
                    .join(member).on(review.memberId.eq(member.id))
                    .where(
                            review.isDeleted.isFalse(),
                            review.memberId.eq(memberId),
                            cafeteria.name.eq(cafeteriaName)
                    )
                    .orderBy(review.id.desc())
                    .limit(3) // 각 카페테리아별 최대 3개
                    .fetch();

            if (!results.isEmpty()) {
                myReviewDetailList.put(cafeteriaName, results);
            }
        }

        return myReviewDetailList;
    }

    @Override
    public Slice<MyReviewDetailRes> findMyReviewsWithImagesAndMemberDetailsByCafeteria(Long memberId, String cafeteriaName, Pageable pageable) {
        QMenu mainMenu = new QMenu("mainMenu");
        QMenu subMenu = new QMenu("subMenu");

        // 쿼리 생성
        List<MyReviewDetailRes> results = jpaQueryFactory
                .select(Projections.constructor(
                        MyReviewDetailRes.class,
                        review.id,
                        review.comment,
                        review.rating,
                        review.likeCount,
                        review.createdDate,
                        menuPair.id,
                        mainMenu.menuName,
                        subMenu.menuName,
                        review.memberId,
                        member.nickname
                ))
                .from(review)
                .join(review.menuPair(), menuPair)
                .join(mainMenu).on(menuPair.mainMenuId.eq(mainMenu.id))
                .leftJoin(subMenu).on(menuPair.subMenuId.eq(subMenu.id))
                .join(cafeteria).on(mainMenu.cafeteriaId.eq(cafeteria.id))
                .join(member).on(review.memberId.eq(member.id))
                .where(
                        review.memberId.eq(memberId),
                        cafeteria.name.eq(cafeteriaName),
                        review.isDeleted.isFalse()
                )
                .orderBy(review.id.desc())
                .offset(pageable.getOffset())   // offset 처리
                .limit(pageable.getPageSize() + 1) // Slice 처리용 +1
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results = results.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    @Override
    public List<BestReviewDto> findMostLikedReviewIdsInMainMenuIds(List<Long> mainMenuIds) {
        // 서브쿼리 1: mainMenuId 별 최대 likeCount 조회
        JPQLSubQuery<Long> maxLikeCountSubquery = JPAExpressions
                .select(review.likeCount.max())
                .from(review)
                .join(review.menuPair(), menuPair)
                .where(
                        menuPair.mainMenuId.eq(review.menuPair().mainMenuId),
                        review.isDeleted.isFalse()
                );

        // 서브쿼리 2: 최대 likeCount인 것 중에서 최대 reviewId 조회
        JPQLSubQuery<Long> maxReviewIdSubquery = JPAExpressions
                .select(review.id.max())
                .from(review)
                .join(review.menuPair(), menuPair)
                .where(
                        menuPair.mainMenuId.eq(review.menuPair().mainMenuId),
                        review.likeCount.eq(maxLikeCountSubquery),
                        review.isDeleted.isFalse()
                );

        // 메인 쿼리
        return jpaQueryFactory
                .select(Projections.constructor(
                        BestReviewDto.class,
                        menuPair.mainMenuId,
                        review.id
                ))
                .from(review)
                .join(review.menuPair(), menuPair)
                .where(
                        review.isDeleted.isFalse(),
                        menuPair.mainMenuId.in(mainMenuIds),
                        review.id.eq(maxReviewIdSubquery)
                )
                .fetch();
    }
}
