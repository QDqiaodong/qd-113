package com.tea.tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ParamSyncRecordResponse {

    private Long id;

    private Long sourceTeaId;
    private String sourceTeaName;
    private Long sourceParamId;
    private String sourceParamName;

    private Long targetTeaId;
    private String targetTeaName;
    private Long targetParamId;
    private String targetParamName;

    private String teaCategory;
    private String syncType;

    private List<FieldDifference> fieldDifferences;

    private LocalDateTime syncedAt;
    private LocalDateTime createdAt;

    @Data
    public static class FieldDifference {
        private String fieldName;
        private String fieldLabel;
        private Object sourceValue;
        private Object targetValue;
    }
}
