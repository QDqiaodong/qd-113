package com.tea.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TastingVocabularyRequest {

    @NotBlank(message = "词汇类型不能为空")
    @Size(max = 20, message = "词汇类型长度不能超过20")
    private String vocabularyType;

    @NotBlank(message = "茶类不能为空")
    @Size(max = 50, message = "茶类长度不能超过50")
    private String teaCategory;

    @NotBlank(message = "词汇不能为空")
    @Size(max = 50, message = "词汇长度不能超过50")
    private String word;

    @Size(max = 200, message = "描述长度不能超过200")
    private String description;

    private Integer sortOrder = 0;
}
