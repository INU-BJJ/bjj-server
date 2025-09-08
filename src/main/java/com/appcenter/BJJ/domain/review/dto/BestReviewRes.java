package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor    // QueryDSL은 접근제어자로 Public 필요
public class BestReviewRes {
    @Schema(description = "리뷰 id", example = "1")
    private final Long reviewId;
    @Schema(description = "리뷰 내용", example = "맛이 참으로 좋읍니다.")
    private final String comment;
    @Schema(description = "리뷰 별점", example = "5")
    private final Integer rating;
    @Schema(description = "리뷰 사진 파일 이름", example = "[\"aa356b24-0169-4c0c-8bf4-836ed3c6b31d.png\", \"72d2efb7-a5d6-439e-93ca-3dd578fa4f67.png\"]")
    @Setter
    private List<String> imageNames;
    @Schema(description = "리뷰 좋아요 개수", example = "123")
    private final Long likeCount;
    @Schema(description = "좋아요 누른 리뷰인지 여부", example = "true")
    private final boolean isLiked;
    @Schema(description = "리뷰 작성일", example = "2024-10-01")
    private final LocalDate createdDate;
    @Schema(description = "메뉴쌍 id", example = "1")
    private final Long menuPairId;
    @Schema(description = "메인메뉴 id", example = "1")
    private final Long mainMenuId;
    @Schema(description = "메인메뉴 이름", example = "등심돈까스")
    private final String mainMenuName;
    @Schema(description = "서브메뉴 id", example = "2")
    private final Long subMenuId;
    @Schema(description = "서브메뉴 이름", example = "쫄면")
    private final String subMenuName;
    @Schema(description = "식당 이름", example = "학생식당")
    private final String cafeteriaName;
    @Schema(description = "식당 코너 이름", example = "중식(백반)")
    private final String cafeteriaCorner;
    @Schema(description = "작성자 id", example = "1")
    private final Long memberId;
    @Schema(description = "작성자 닉네임", example = "이춘삼")
    private final String memberNickname;
    @Schema(description = "작성자 프로필 사진 파일 이름", example = "23fsddfesff=3vlsdd-3sdf56.png")
    private String memberImageName;
    @Schema(description = "리뷰를 조회하는 유저가 리뷰를 작성한 유저인지 여부", example = "true")
    private final boolean isOwned;

    public BestReviewRes() {
        this.reviewId = null;
        this.comment = null;
        this.rating = null;
        this.imageNames = null;
        this.likeCount = null;
        this.isLiked = false;
        this.createdDate = null;
        this.menuPairId = null;
        this.mainMenuId = null;
        this.mainMenuName = null;
        this.subMenuId = null;
        this.subMenuName = null;
        this.cafeteriaName = null;
        this.cafeteriaCorner = null;
        this.memberId = null;
        this.memberNickname = null;
        this.memberImageName = null;
        this.isOwned = false;
    }
}