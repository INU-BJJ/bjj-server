package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewDetailRes {
    @Schema(description = "리뷰 id", example = "1")
    private final Long reviewId;
    @Schema(description = "리뷰 내용", example = "맛이 참으로 좋읍니다.")
    private final String comment;
    @Schema(description = "리뷰 별점", example = "5")
    private final Integer rating;
    @Schema(description = "리뷰 이미지 경로", example = "C:\\BJJ\\ReviewImages\\23fsddfesff=3vlsdd-3sdf56.png")
    private final List<String> imagePaths;
    @Schema(description = "리뷰 좋아요 개수", example = "123")
    private final Long likeCount;
    @Schema(description = "좋아하는 메뉴 여부", example = "true")
    private final boolean isLikedMenu;
    @Schema(description = "리뷰 작성일", example = "2024-10-01")
    private final LocalDate createdDate;
    @Schema(description = "메뉴쌍 id", example = "1")
    private final Long menuPairId;
    @Schema(description = "메인메뉴 id", example = "1")
    private final Long mainMenuId;
    @Schema(description = "서브메뉴 id", example = "1")
    private final Long subMenuId;
    @Schema(description = "회원 id", example = "1")
    private final Long memberId;
    @Schema(description = "회원 닉네임", example = "이춘삼")
    private final String memberNickname;
    @Schema(description = "회원 프로필 이미지 경로", example = "C:\\BJJ\\ReviewImages\\23fsddfesff=3vlsdd-3sdf56.png")
    private final String memberImagePath;
}
