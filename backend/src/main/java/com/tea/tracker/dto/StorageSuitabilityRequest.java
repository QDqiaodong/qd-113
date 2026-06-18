package com.tea.tracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StorageSuitabilityRequest {

    private String storageMethod;

    @DecimalMin(value = "-50", message = "温度不能低于-50℃")
    @DecimalMax(value = "80", message = "温度不能高于80℃")
    private BigDecimal temperature;

    @DecimalMin(value = "0", message = "湿度不能低于0%")
    @DecimalMax(value = "100", message = "湿度不能高于100%")
    private BigDecimal humidity;

    private String sealCondition;
}
