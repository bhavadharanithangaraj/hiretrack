package com.hiretrack.dto;

import java.time.Instant;
import java.util.Map;

// Every error from the API has this same JSON shape, so the frontend can handle them in one place.
// fieldErrors is only filled for validation errors, e.g. { "company": "Company is required" }.
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, Map.of());
    }
}
