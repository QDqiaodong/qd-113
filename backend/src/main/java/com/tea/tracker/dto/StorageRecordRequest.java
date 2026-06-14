package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StorageRecordRequest {

    private String storageLocation;

    private BigDecimal temperature;

    private BigDecimal humidity;

    private String sealCondition;

    private BigDecimal stockChange;

    private BigDecimal currentStock;

    private String notes;
}
