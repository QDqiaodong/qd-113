package com.tea.tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TemplateVersionResponse {

    private Long id;
    private String teaCategory;
    private Long version;
    private String paramSource;
    private LocalDateTime updatedAt;
}
