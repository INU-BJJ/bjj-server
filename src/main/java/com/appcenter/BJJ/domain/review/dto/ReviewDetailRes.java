package com.appcenter.BJJ.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewDetailRes {
    @Schema(description = "리뷰 id", example = "1")
    private final Long reviewId;
    @Schema(description = "리뷰 내용", example = "맛이 참으로 좋읍니다.")
    private final String comment;
    @Schema(description = "리뷰 별점", example = "5")
    private final Integer rating;
    @Schema(description = "리뷰 이미지 이름", example = "23fsddfesff=3vlsdd-3sdf56.png")
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
    @Schema(description = "메인메뉴 이름", example = "등심돈까스")
    private final String mainMenuName;
    @Schema(description = "서브메뉴 이름", example = "쫄면")
    private final String subMenuName;
    @Schema(description = "작성자 id", example = "1")
    private final Long memberId;
    @Schema(description = "작성자 닉네임", example = "이춘삼")
    private final String memberNickname;
    @Schema(description = "작성자 프로필 이미지 이름", example = "23fsddfesff=3vlsdd-3sdf56.png")
    private String memberImageName;
}
