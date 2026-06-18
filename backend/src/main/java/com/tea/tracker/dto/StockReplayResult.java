package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockReplayResult {

    private Long teaId;
    private String teaName;
    private BigDecimal previousStock;
    private BigDecimal recalculatedStock;
    private int totalRecordsReplayed;
    private List<StorageRecordResponse> replayedRecords;
}
