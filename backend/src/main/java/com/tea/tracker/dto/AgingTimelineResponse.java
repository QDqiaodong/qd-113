package com.tea.tracker.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgingTimelineResponse {

    private Long teaId;
    private String teaName;
    private String teaCategory;
    private Integer harvestYear;
    private String storageMethod;
    private Integer currentAgingYears;
    private Integer totalAgingMonths;
    private String agingStatus;
    private String agingDescription;
    private Boolean isAgingRecommended;
    private Integer optimalAgingStartYears;
    private Integer optimalAgingEndYears;
    private List<AgingNode> nodes;
    private LocalDateTime calculatedAt;
}
