package com.appcenter.BJJ.domain.review.service;

import com.appcenter.BJJ.domain.member.MemberRepository;
import com.appcenter.BJJ.domain.member.domain.Member;
import com.appcenter.BJJ.domain.menu.domain.MenuPair;
import com.appcenter.BJJ.domain.menu.repository.MenuPairRepository;
import com.appcenter.BJJ.domain.review.domain.Review;
import com.appcenter.BJJ.domain.review.repository.ReviewRepository;
import com.appcenter.BJJ.domain.review.utils.ReviewPolicy;
import com.appcenter.BJJ.domain.todaydiet.repository.CafeteriaRepository;
import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.appcenter.BJJ.domain.review.dto.ReviewReq.ReviewPost;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    @Mock
    private MenuPairRepository menuPairRepository;
    @Mock
    private CafeteriaRepository cafeteriaRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReviewPolicy reviewPolicy;
    @InjectMocks
    private ReviewService reviewService;

    private final String CAFETERIA_CORNER = "조식";
    private final Long MEMBER_ID = 100L;
    private final Long MENU_PAIR_ID = 100L;

    private ReviewPost createReviewPost() {
        return fixtureMonkey.giveMeBuilder(ReviewPost.class)
                .set("menuPairId", 1L)
                .set("comment", "맛있어요")
                .set("rating", 5)
                .sample();
    }

    private ReviewPost createReviewPostWithMenuPairId(Long menuPairId) {
        return fixtureMonkey.giveMeBuilder(ReviewPost.class)
                .set("menuPairId", menuPairId)
                .set("comment", "맛있어요")
                .set("rating", 5)
                .sample();
    }

    private Member createMemberWithPoint(Long memberId, int point) {
        return fixtureMonkey.giveMeBuilder(Member.class)
                .set("id", memberId)
                .set("point", point)
                .sample();
    }

    private Review createReview(Long id, Member member, MenuPair menuPair, ReviewPost reviewPost) {
        return fixtureMonkey.giveMeBuilder(Review.class)
                .set("id", id)
                .set("member", member)
                .set("menuPair", menuPair)
                .set("comment", reviewPost.getComment())
                .set("rating", reviewPost.getRating())
                .sample();
    }

    @Test
    void create_MenuPairNotFound_ThrowsException() {
        // given
        ReviewPost reviewPost = createReviewPostWithMenuPairId(MENU_PAIR_ID);
        when(menuPairRepository.findById(MENU_PAIR_ID)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.create(reviewPost, null, null));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_PAIR_NOT_FOUND);
    }

    @Test
    void create_CafeteriaNotFound_ThrowsException() {
        // given
        ReviewPost reviewPost = createReviewPostWithMenuPairId(MENU_PAIR_ID);
        when(menuPairRepository.findById(MENU_PAIR_ID)).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(MENU_PAIR_ID)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.create(reviewPost, null, null));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CAFETERIA_NOT_FOUND);
    }

    @Test
    void create_InvalidReviewTime_ThrowsException() {
        // given
        String cafeteriaCorner = "조식";
        LocalTime breakfastStart = LocalTime.of(8, 0);
        ReviewPost reviewPost = createReviewPost();
        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(cafeteriaCorner));
        when(reviewPolicy.isReviewableTime(
                eq(cafeteriaCorner),
                argThat(time -> time.isBefore(breakfastStart))
        )).thenReturn(false);  // 조식이면서 8시 이전(리뷰 불가능 시간)일 때 false로 설정

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.create(reviewPost, null, null));
        assertEquals(ErrorCode.INVALID_REVIEW_TIME, exception.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"조식", "중식", "석식"})
    void create_ReviewAlreadyExists_ThrowsException_ForCafeteriaCorner(String cafeteriaCorner) {
        // given
        ReviewPost reviewPost = createReviewPost();
        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(cafeteriaCorner)); // 각 값에 대해 테스트
        when(reviewPolicy.isReviewableTime(anyString(), any(LocalTime.class))).thenReturn(true);

        // 각 'cafeteriaCorner'에 맞는 메서드만 Mocking
        if ("조식".equals(cafeteriaCorner) || "석식".equals(cafeteriaCorner)) {
            when(reviewRepository.existsTodayUndeletedReviewByCafeteriaCorner(cafeteriaCorner)).thenReturn(true);
        } else {
            when(reviewRepository.existsTodayUndeletedReviewExcludingBreakfastAndDinner()).thenReturn(true);
        }

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.create(reviewPost, null, null));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"조식", "중식", "석식"})
    void create_CheckTodayReviewExistence_ForCafeteriaCorner(String cafeteriaCorner) {
        // given
        ReviewPost reviewPost = createReviewPost();
        Review review = createReview(1L, createMemberWithPoint(MEMBER_ID, 0), MenuPair.builder().build(), reviewPost);
        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(cafeteriaCorner));
        when(reviewPolicy.isReviewableTime(anyString(), any(LocalTime.class))).thenReturn(true);

        // 각 'cafeteriaCorner'에 맞는 메서드만 Mocking
        if ("조식".equals(cafeteriaCorner) || "석식".equals(cafeteriaCorner)) {
            when(reviewRepository.existsTodayUndeletedReviewByCafeteriaCorner(cafeteriaCorner)).thenReturn(false);
            when(reviewRepository.existsTodayReviewByCafeteriaCorner(cafeteriaCorner)).thenReturn(true);
        } else {
            when(reviewRepository.existsTodayUndeletedReviewExcludingBreakfastAndDinner()).thenReturn(false);
            when(reviewRepository.existsTodayReviewExcludingBreakfastAndDinner()).thenReturn(true);
        }

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // when
        reviewService.create(reviewPost, null, MEMBER_ID);  // 리뷰 생성 메서드 호출

        // then
        if ("조식".equals(cafeteriaCorner) || "석식".equals(cafeteriaCorner)) {
            // 조식/석식일 경우 'existsTodayReviewByCafeteriaCorner'가 호출되었는지 검증
            verify(reviewRepository).existsTodayReviewByCafeteriaCorner(cafeteriaCorner);
        } else {
            // 중식일 경우 'existsTodayReviewExcludingBreakfastAndDinner'가 호출되었는지 검증
            verify(reviewRepository).existsTodayReviewExcludingBreakfastAndDinner();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"조식", "중식", "석식"})
    void create_UserNotFound_ThrowsException(String cafeteriaCorner) {
        // given
        ReviewPost reviewPost = createReviewPost();
        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(cafeteriaCorner));
        when(reviewPolicy.isReviewableTime(anyString(), any(LocalTime.class))).thenReturn(true);

        // 각 'cafeteriaCorner'에 맞는 메서드만 Mocking
        if ("조식".equals(cafeteriaCorner) || "석식".equals(cafeteriaCorner)) {
            when(reviewRepository.existsTodayUndeletedReviewByCafeteriaCorner(cafeteriaCorner)).thenReturn(false);
            when(reviewRepository.existsTodayReviewByCafeteriaCorner(cafeteriaCorner)).thenReturn(false);
        } else {
            when(reviewRepository.existsTodayUndeletedReviewExcludingBreakfastAndDinner()).thenReturn(false);
            when(reviewRepository.existsTodayReviewExcludingBreakfastAndDinner()).thenReturn(false);
        }

        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.create(reviewPost, null, MEMBER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void create_InvalidImageFileName_ThrowsException(@TempDir Path tempDir) {
        // given
        ReviewPost reviewPost = createReviewPost();
        MultipartFile invalidFile = new MockMultipartFile("file", "", "image/jpeg", new byte[1024]);

        ReflectionTestUtils.setField(reviewService, "REVIEW_IMG_DIR", tempDir.toString());  // 임시 디렉토리를 서비스에 주입

        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(CAFETERIA_CORNER));
        when(reviewPolicy.isReviewableTime(anyString(), any(LocalTime.class))).thenReturn(true);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(createMemberWithPoint(MEMBER_ID, 0)));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> reviewService.create(reviewPost, List.of(invalidFile), MEMBER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    void create_ReviewWithPhoto_Increases100PointsAndSavesReview(@TempDir Path tempDir) {
        // given
        final long reviewId = 100L; // 생성될 리뷰 ID
        ReflectionTestUtils.setField(reviewService, "REVIEW_IMG_DIR", tempDir.toString());  // 임시 디렉토리를 서비스에 주입

        ReviewPost reviewPost = createReviewPost();
        Member member = createMemberWithPoint(MEMBER_ID, 0);
        MultipartFile file = new MockMultipartFile(
                "file",                   // parameter name
                "test.jpg",               // original filename
                "image/jpeg",             // content type
                new byte[1024]            // content
        );

        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(CAFETERIA_CORNER));
        when(reviewPolicy.isReviewableTime(anyString(), any(LocalTime.class))).thenReturn(true);
        when(reviewRepository.existsTodayUndeletedReviewByCafeteriaCorner(anyString())).thenReturn(false);
        when(reviewRepository.existsTodayReviewByCafeteriaCorner(anyString())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(reviewRepository.save(any())).thenReturn(
                createReview(reviewId, member, MenuPair.builder().build(), reviewPost)
        );

        // when
        Long createdReviewId = reviewService.create(reviewPost, List.of(file), MEMBER_ID);

        // then
        assertEquals(100, member.getPoint()); // 100 포인트 증가 확인
        assertEquals(reviewId, createdReviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void create_ReviewWithoutPhoto_Increase50PointsAndSavesReview() {
        // given
        final long reviewId = 100L; // 생성될 리뷰 ID
        ReviewPost reviewPost = createReviewPost();
        Member member = createMemberWithPoint(MEMBER_ID, 0);

        when(menuPairRepository.findById(anyLong())).thenReturn(Optional.of(MenuPair.builder().build()));
        when(cafeteriaRepository.findCafeteriaCornerByMenuPairId(anyLong())).thenReturn(Optional.of(CAFETERIA_CORNER));
        when(reviewPolicy.isReviewableTime(anyString(), any(LocalTime.class))).thenReturn(true);
        when(reviewRepository.existsTodayUndeletedReviewByCafeteriaCorner(anyString())).thenReturn(false);
        when(reviewRepository.existsTodayReviewByCafeteriaCorner(anyString())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(reviewRepository.save(any(Review.class))).thenReturn(
                createReview(reviewId, member, MenuPair.builder().build(), reviewPost)
        );

        // when
        Long createdReviewId = reviewService.create(reviewPost, null, MEMBER_ID);

        // then
        assertEquals(50, member.getPoint()); // 50 포인트 증가 확인
        assertEquals(reviewId, createdReviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
}