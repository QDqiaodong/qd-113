package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StorageRecordResponse {

    private Long id;
    private Long teaId;
    private String teaName;
    private String storageLocation;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String sealCondition;
    private BigDecimal stockChange;
    private BigDecimal currentStock;
    private LocalDateTime recordDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
