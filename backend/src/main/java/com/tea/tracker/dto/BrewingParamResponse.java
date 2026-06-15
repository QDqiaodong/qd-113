package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class BrewingParamResponse {

    private Long id;
    private Long teaId;
    private String teaName;
    private String paramName;
    private Integer waterTemperature;
    private BigDecimal teaAmount;
    private String teaRatio;
    private BigDecimal waterAmount;
    private Integer firstInfusionTime;
    private Integer secondInfusionTime;
    private Integer thirdInfusionTime;
    private Integer subsequentInfusionTime;
    private Integer totalInfusions;
    private String waterQuality;
    private String notes;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Map<String, ParamDeviation> deviations;

    @Data
    public static class ParamDeviation {
        private boolean deviates;
        private Object templateValue;
        private Object actualValue;
        private String direction;
        private Double deviationPercent;
    }
}
