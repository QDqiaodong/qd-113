package com.tea.tracker.dto;

import lombok.Data;

import java.util.List;

@Data
public class BrewingParamSyncRequest {

    private Long sourceTeaId;

    private Long sourceParamId;

    private Long targetTeaId;

    private String targetParamName;

    private Boolean setAsDefault = false;

    private List<String> fieldsToSync;
}
