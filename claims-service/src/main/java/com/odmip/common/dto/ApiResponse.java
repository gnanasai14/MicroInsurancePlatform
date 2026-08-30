package com.odmip.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ignoreUnknown = true because this class is also used to DESERIALIZE
 * responses coming back from user-service, whose ApiResponse envelope
 * includes an extra "timestamp" field this class doesn't declare.
 * Without this, Jackson throws UnrecognizedPropertyException on every
 * response from user-service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
}
