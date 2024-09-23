package com.appcenter.BJJ.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewRes {

    private final List<ReviewDetailRes> reviewDetailList;

    private final boolean isLastPage;
}
