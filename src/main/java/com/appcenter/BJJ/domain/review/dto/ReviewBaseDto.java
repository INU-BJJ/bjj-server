package com.appcenter.BJJ.domain.review.dto;

import java.util.List;

public abstract class ReviewBaseDto {

    public abstract Long getReviewId();
    public abstract Long getMemberId();

    public abstract ReviewBaseDto withImageNames(List<String> imageNames);
    public abstract ReviewBaseDto withMemberImageName(String memberImageName);
}
