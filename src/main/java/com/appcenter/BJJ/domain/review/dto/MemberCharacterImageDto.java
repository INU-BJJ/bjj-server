package com.appcenter.BJJ.domain.review.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberCharacterImageDto {
    private Long memberId;
    private String imageName;
}
