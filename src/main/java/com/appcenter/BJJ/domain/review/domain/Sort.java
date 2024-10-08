package com.appcenter.BJJ.domain.review.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Sort {
    BEST_MATCH("BestMatch"),
    MOST_LIKED("MostLiked"),
    NEWEST_FIRST("NewestFirst");

    private final String input;

    Sort(String input) {
        this.input = input;
    }

    public static Sort getSort(String inputString) {
        return Arrays.stream(values())
                .filter(sort -> sort.getInput().equals(inputString))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Sort 값이 잘못 되었습니다. 올바른 값을 입력해주세요."));
    }
}
