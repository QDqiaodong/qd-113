package com.tea.tracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrewingSessionRequest {

    private Long brewingParamId;

    @Min(value = 0, message = "实际水温不能低于0℃")
    @Max(value = 100, message = "实际水温不能高于100℃")
    private Integer actualWaterTemperature;

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

    private String tasteImpression;

    private Boolean deductStock;

    @DecimalMin(value = "0", message = "扣减数量不能为负数")
    @DecimalMax(value = "99999.99", message = "扣减数量不能超过99999.99")
    private BigDecimal stockAmount;
}
