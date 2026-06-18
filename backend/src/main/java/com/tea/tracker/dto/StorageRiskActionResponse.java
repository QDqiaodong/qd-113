package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StorageRiskActionResponse {

    private Long id;
    private Long teaId;
    private String teaName;
    private Long suitabilityId;
    private Long storageRecordId;
    private String riskType;
    private String riskLevel;
    private String riskDescription;
    private BigDecimal temperatureBefore;
    private BigDecimal humidityBefore;
    private String sealBefore;
    private String actionType;
    private String actionDescription;
    private String actionBy;
    private LocalDateTime actionDate;
    private BigDecimal temperatureAfter;
    private BigDecimal humidityAfter;
    private String sealAfter;
    private String resultStatus;
    private String resultDescription;
    private LocalDateTime followUpDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
