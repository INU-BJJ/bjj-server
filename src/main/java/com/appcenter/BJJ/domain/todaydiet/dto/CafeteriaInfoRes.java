package com.appcenter.BJJ.domain.todaydiet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CafeteriaInfoRes {

    @Schema(description = "식당 이름", example = "학생식당")
    private final String name;

    @Schema(description = "위치 정보", example = "11호관 (복지회관) 1층")
    private final String location;

    @Schema(description = "운영 시간 정보")
    private final OperationTimeDto operationTime;

    @Schema(description = "사진 파일 이름", example = "cafeteria_01.png")
    private final String imageName;

    @Getter
    public static class OperationTimeDto {
        private String operation;

        private List<String> weekdays;

        private List<String> weekends;
    }
}
