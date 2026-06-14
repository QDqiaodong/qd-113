package com.tea.tracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrewingParamRequest {

    private String paramName;

    @NotNull(message = "水温不能为空")
    private Integer waterTemperature;

    @NotNull(message = "投茶量不能为空")
    private BigDecimal teaAmount;

    private String teaRatio;

    private BigDecimal waterAmount;

    @NotNull(message = "首泡出汤时长不能为空")
    private Integer firstInfusionTime;

    private Integer secondInfusionTime;

    private Integer thirdInfusionTime;

    private Integer subsequentInfusionTime;

    private Integer totalInfusions;

    private String waterQuality;

    private String notes;

    private Boolean isDefault = false;
}
