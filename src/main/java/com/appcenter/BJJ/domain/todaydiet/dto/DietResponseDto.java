package com.appcenter.BJJ.domain.todaydiet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class DietResponseDto {
    @JsonProperty("diet")
    private List<DietDto> diet;
}
