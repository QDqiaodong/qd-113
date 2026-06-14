package com.tea.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private Integer harvestYear;

    private String storageMethod;

    private BigDecimal currentStock;

    private String stockUnit = "克";

    private String description;

    private String imageUrl;
}
