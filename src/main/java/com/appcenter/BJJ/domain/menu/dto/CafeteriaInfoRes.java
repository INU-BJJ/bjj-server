package com.appcenter.BJJ.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafeteriaInfoRes {

    @Schema(description = "이름", example = "학생 식당")
    private final String name;

    @Schema(description = "위치 정보", example = "11호관 (복지회관) 1층")
    private final String location;

    @Schema(description = "운영 시간", example = "운영시간 (학기 중) - 평일 : 중식 11:00~13:30 석식 17:00~18:10 / 주말 : 휴점")
    private final String operationTime;

    @Schema(description = "사진 파일명", example = "cafeteria_1.png")
    private final String imageName;
}
