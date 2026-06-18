package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeaResponse {

    private Long id;
    private String name;
    private String teaCategory;
    private String originRegion;
    private Integer harvestYear;
    private String yearNote;
    private String storageMethod;
    private BigDecimal currentStock;
    private String stockUnit;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastStorageDate;
    private List<StockTrendPoint> stockTrend;
    private List<BrewingParamResponse> brewingParams;
    private List<StorageRecordResponse> storageRecords;
    private List<TastingNoteResponse> tastingNotes;
}
