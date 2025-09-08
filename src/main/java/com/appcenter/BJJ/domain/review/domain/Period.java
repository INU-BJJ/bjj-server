package com.appcenter.BJJ.domain.review.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

public enum Period {
    DAY {
        @Override
        public LocalDate getFromDate(LocalDate today) {
            return today;
        }
    },
    WEEK {
        @Override
        public LocalDate getFromDate(LocalDate today) {
            return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
    },
    MONTH {
        @Override
        public LocalDate getFromDate(LocalDate today) {
            return today.with(TemporalAdjusters.firstDayOfMonth());
        }
    },
    SEMESTER {
        @Override
        public LocalDate getFromDate(LocalDate today) {
            // 3~8월은 1학기, 9~2월은 2학기
            int year = today.getYear();
            int month = today.getMonth().getValue();

            if (month >= 3 && month <= 8) {
                return LocalDate.of(year, Month.MARCH, 1);
            } else {
                if (month <= 2) year -= 1;
                return LocalDate.of(year, Month.SEPTEMBER, 1);
            }
        }
    };

    public abstract LocalDate getFromDate(LocalDate today);
}
