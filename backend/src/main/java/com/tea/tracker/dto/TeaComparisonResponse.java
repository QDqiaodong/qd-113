package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeaComparisonResponse {

    private Long id;
    private String name;
    private String teaCategory;
    private String originRegion;
    private Integer harvestYear;
    private String storageMethod;
    private BigDecimal currentStock;
    private String stockUnit;
    private String description;
    private String imageUrl;

    private BrewingParamResponse defaultBrewingParam;
    private StorageRecordResponse latestStorageRecord;
    private TastingScoreSummary avgTastingScores;
}
