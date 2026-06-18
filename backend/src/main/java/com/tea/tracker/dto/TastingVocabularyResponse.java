package com.tea.tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TastingVocabularyResponse {

    private Long id;
    private String vocabularyType;
    private String teaCategory;
    private String word;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
