package com.tea.tracker.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TastingNoteResponse {

    private Long id;
    private Long teaId;
    private String teaName;
    private String brewingMethod;
    private Integer aromaScore;
    private String aromaDesc;
    private Integer liquorColorScore;
    private String liquorColorDesc;
    private Integer tasteScore;
    private String tasteDesc;
    private Integer aftertasteScore;
    private String aftertasteDesc;
    private Integer overallScore;
    private String impression;
    private LocalDateTime tastingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
