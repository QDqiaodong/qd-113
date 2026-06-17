package com.tea.tracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StorageRecordRequest {

    private String storageLocation;

    @DecimalMin(value = "-50", message = "温度不能低于-50℃")
    @DecimalMax(value = "80", message = "温度不能高于80℃")
    private BigDecimal temperature;

    @DecimalMin(value = "0", message = "湿度不能低于0%")
    @DecimalMax(value = "100", message = "湿度不能高于100%")
    private BigDecimal humidity;

    private String sealCondition;

    @DecimalMin(value = "-99999.99", message = "库存变化不能低于-99999.99")
    @DecimalMax(value = "99999.99", message = "库存变化不能高于99999.99")
    private BigDecimal stockChange;

    private String notes;
}
