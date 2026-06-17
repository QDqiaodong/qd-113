package com.tea.tracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrewingSessionRequest {

    private Long brewingParamId;

    private Integer actualWaterTemperature;

    private Integer firstInfusionTime;

    private Integer secondInfusionTime;

    private Integer thirdInfusionTime;

    private Integer subsequentInfusionTime;

    private String tasteImpression;
}
