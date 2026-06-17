package com.tea.tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TastingNoteRequest {

    private String brewingMethod;

    @Min(value = 0, message = "香气评分不能低于0分")
    @Max(value = 100, message = "香气评分不能高于100分")
    private Integer aromaScore;

    private String aromaDesc;

    @Min(value = 0, message = "汤色评分不能低于0分")
    @Max(value = 100, message = "汤色评分不能高于100分")
    private Integer liquorColorScore;

    private String liquorColorDesc;

    @Min(value = 0, message = "滋味评分不能低于0分")
    @Max(value = 100, message = "滋味评分不能高于100分")
    private Integer tasteScore;

    private String tasteDesc;

    @Min(value = 0, message = "回甘评分不能低于0分")
    @Max(value = 100, message = "回甘评分不能高于100分")
    private Integer aftertasteScore;

    private String aftertasteDesc;

    @Min(value = 0, message = "综合评分不能低于0分")
    @Max(value = 100, message = "综合评分不能高于100分")
    private Integer overallScore;

    private String impression;
}
