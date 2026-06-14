package com.tea.tracker.dto;

import lombok.Data;

@Data
public class TastingNoteRequest {

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
}
