package com.tea.tracker.exception;

import com.tea.tracker.dto.FieldValidationError;
import lombok.Getter;

import java.util.List;

@Getter
public class BrewingParamValidationException extends RuntimeException {

    private final List<FieldValidationError> errors;

    public BrewingParamValidationException(String message, List<FieldValidationError> errors) {
        super(message);
        this.errors = errors;
    }
}
