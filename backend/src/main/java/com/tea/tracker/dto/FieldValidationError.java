package com.tea.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldValidationError {

    private String field;
    private Object value;
    private Object min;
    private Object max;
    private String message;
}
