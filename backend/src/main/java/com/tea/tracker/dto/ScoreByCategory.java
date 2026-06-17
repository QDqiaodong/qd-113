package com.tea.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreByCategory {

    private String teaCategory;
    private BigDecimal avgAromaScore;
    private BigDecimal avgLiquorColorScore;
    private BigDecimal avgTasteScore;
    private BigDecimal avgAftertasteScore;
    private BigDecimal avgOverallScore;
    private Long noteCount;
}
