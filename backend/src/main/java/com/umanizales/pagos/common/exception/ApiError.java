package com.umanizales.pagos.common.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    Instant timestamp,
    int status,
    String message,
    Map<String, String> errors
) {
    public ApiError(int status, String message) {
        this(Instant.now(), status, message, null);
    }

    public ApiError(int status, String message, Map<String, String> errors) {
        this(Instant.now(), status, message, errors);
    }
}
