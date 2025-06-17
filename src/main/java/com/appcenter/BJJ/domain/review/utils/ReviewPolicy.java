package com.appcenter.BJJ.domain.review.utils;

import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class ReviewPolicy {
    public static final LocalTime BREAKFAST_START = LocalTime.of(8, 0);
    private static final LocalTime LUNCH_START = LocalTime.of(10, 30);
    private static final LocalTime DINNER_START = LocalTime.of(17, 0);

    public boolean isReviewableTime(String cafeteriaCorner, LocalTime time) {
        if (time.isBefore(BREAKFAST_START)) {
            return false;
        } else if (time.isBefore(LUNCH_START)) {
            return cafeteriaCorner.contains("조식");
        } else if (time.isBefore(DINNER_START)) {
            return !cafeteriaCorner.contains("석식");
        } else {
            return true;
        }
    }
}
