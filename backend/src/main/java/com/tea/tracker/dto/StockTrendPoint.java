package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockTrendPoint {
    private LocalDateTime recordDate;
    private BigDecimal stock;
    private BigDecimal change;
}
