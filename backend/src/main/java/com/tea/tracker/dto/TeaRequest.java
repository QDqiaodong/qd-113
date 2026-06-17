package com.tea.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeaRequest {

    @NotBlank(message = "茶品名称不能为空")
    private String name;

    @NotBlank(message = "茶类不能为空")
    private String teaCategory;

    @NotBlank(message = "产区不能为空")
    private String originRegion;

    @Min(value = 1900, message = "年份不能早于1900年")
    @Max(value = 2100, message = "年份不能晚于2100年")
    private Integer harvestYear;

    private String storageMethod;

    @DecimalMin(value = "0", message = "库存不能为负数")
    private BigDecimal currentStock;

    private String stockUnit = "克";

    private String description;

    private String imageUrl;
}
