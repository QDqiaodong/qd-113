package com.tea.tracker.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AgingNode {

    private Long id;
    private String nodeType;
    private String nodeTitle;
    private String nodeDescription;
    private LocalDate nodeDate;
    private Integer yearsSinceHarvest;
    private Integer monthsSinceHarvest;
    private String importance;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String storageLocation;
    private String sealCondition;
    private String relatedRecordType;
    private Long relatedRecordId;
    private Boolean isHighlighted;
    private String color;
}
