package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BrewingSessionResponse {

    private Long id;
    private Long teaId;
    private String teaName;
    private Long brewingParamId;
    private String brewingParamName;
    private Integer actualWaterTemperature;
    private Integer firstInfusionTime;
    private Integer secondInfusionTime;
    private Integer thirdInfusionTime;
    private Integer subsequentInfusionTime;
    private String tasteImpression;
    private Boolean stockDeducted;
    private BigDecimal stockAmount;
    private Long storageRecordId;
    private BigDecimal currentStockAfterDeduction;
    private String stockUnit;
    private LocalDateTime sessionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
