package com.appcenter.BJJ.domain.review.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReviewPolicyTest {

    private final ReviewPolicy reviewPolicy = new ReviewPolicy();

    private final String BREAKFAST_CORNER = "조식 코너";
    private final String LUNCH_CORNER = "중식 코너";
    private final String DINNER_CORNER = "석식 코너";

    @Test
    void isReviewableTime_BeforeBreakfast_ReturnsFalse() {
        // given
        LocalTime time = LocalTime.of(7, 59);

        // when
        boolean result = reviewPolicy.isReviewableTime(BREAKFAST_CORNER, time);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isReviewableTime_BetweenBreakfastAndLunch_CornerContainsBreakfast_ReturnsTrue() {
        // given
        LocalTime time = LocalTime.of(8, 0);

        // when
        boolean result = reviewPolicy.isReviewableTime(BREAKFAST_CORNER, time);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isReviewableTime_BetweenBreakfastAndLunch_CornerDoesNotContainBreakfast_ReturnsFalse() {
        // given
        LocalTime time = LocalTime.of(8, 0);

        // when
        boolean result = reviewPolicy.isReviewableTime(LUNCH_CORNER, time);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isReviewableTime_BetweenLunchAndDinner_CornerDoesNotContainDinner_ReturnsTrue() {
        // given
        LocalTime time = LocalTime.of(10, 30);

        // when
        boolean result = reviewPolicy.isReviewableTime(LUNCH_CORNER, time);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isReviewableTime_BetweenLunchAndDinner_CornerContainsDinner_ReturnsFalse() {
        // given
        LocalTime time = LocalTime.of(10, 30);

        // when
        boolean result = reviewPolicy.isReviewableTime(DINNER_CORNER, time);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isReviewableTime_AfterDinnerStart_ReturnsTrue() {
        // given
        LocalTime time = LocalTime.of(17, 0);

        // when
        boolean result = reviewPolicy.isReviewableTime(DINNER_CORNER, time);

        // then
        assertThat(result).isTrue();
    }
}
