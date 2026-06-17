package com.tea.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrewingParamRequest {

    private String paramName;

    private String brewingMethod;

    @NotNull(message = "水温不能为空")
    @Min(value = 0, message = "水温不能低于0℃")
    @Max(value = 100, message = "水温不能高于100℃")
    private Integer waterTemperature;

    @NotNull(message = "投茶量不能为空")
    @DecimalMin(value = "0", message = "投茶量不能为负数")
    @DecimalMax(value = "999.99", message = "投茶量不能超过999.99克")
    private BigDecimal teaAmount;

    private String teaRatio;

    @DecimalMin(value = "0", message = "水量不能为负数")
    @DecimalMax(value = "9999.99", message = "水量不能超过9999.99毫升")
    private BigDecimal waterAmount;

    @NotNull(message = "首泡出汤时长不能为空")
    @Min(value = 0, message = "首泡出汤时长不能为负数")
    @Max(value = 3600, message = "首泡出汤时长不能超过3600秒")
    private Integer firstInfusionTime;

    @Min(value = 0, message = "第二泡出汤时长不能为负数")
    @Max(value = 3600, message = "第二泡出汤时长不能超过3600秒")
    private Integer secondInfusionTime;

    @Min(value = 0, message = "第三泡出汤时长不能为负数")
    @Max(value = 3600, message = "第三泡出汤时长不能超过3600秒")
    private Integer thirdInfusionTime;

    @Min(value = 0, message = "后续泡出汤时长不能为负数")
    @Max(value = 3600, message = "后续泡出汤时长不能超过3600秒")
    private Integer subsequentInfusionTime;

    @Min(value = 0, message = "冲泡次数不能为负数")
    @Max(value = 100, message = "冲泡次数不能超过100次")
    private Integer totalInfusions;

    private String waterQuality;

    private String notes;

    private Boolean isDefault = false;
}
