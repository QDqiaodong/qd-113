package com.tea.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StorageRiskActionRequest {

    private Long suitabilityId;

    private Long storageRecordId;

    @NotBlank(message = "风险类型不能为空")
    @Size(max = 50, message = "风险类型长度不能超过50")
    private String riskType;

    @NotBlank(message = "风险等级不能为空")
    @Size(max = 20, message = "风险等级长度不能超过20")
    private String riskLevel;

    @Size(max = 500, message = "风险描述长度不能超过500")
    private String riskDescription;

    private BigDecimal temperatureBefore;

    private BigDecimal humidityBefore;

    private String sealBefore;

    @Size(max = 100, message = "处置方式长度不能超过100")
    private String actionType;

    @Size(max = 1000, message = "处置描述长度不能超过1000")
    private String actionDescription;

    @Size(max = 100, message = "处置人长度不能超过100")
    private String actionBy;

    private LocalDateTime actionDate;

    private BigDecimal temperatureAfter;

    private BigDecimal humidityAfter;

    private String sealAfter;

    @Size(max = 20, message = "结果状态长度不能超过20")
    private String resultStatus;

    @Size(max = 500, message = "结果描述长度不能超过500")
    private String resultDescription;

    private LocalDateTime followUpDate;
}
