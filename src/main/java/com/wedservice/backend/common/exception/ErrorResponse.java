package com.wedservice.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private Map<String, String> errors;
}
