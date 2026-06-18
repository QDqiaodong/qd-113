package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StorageSuitabilityResponse {

    private Long id;
    private Long teaId;
    private String teaName;
    private String teaCategory;
    private String storageMethod;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String sealCondition;
    private Integer totalScore;
    private Integer temperatureScore;
    private Integer humidityScore;
    private Integer sealScore;
    private Integer storageMethodScore;
    private String suitabilityLevel;
    private String suggestions;
    private LocalDateTime recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean hasRisk;
    private String riskLevel;
    private List<String> abnormalItems;
}
