package com.odmip.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Local stand-in for the shared `common` module's ApiResponse<T>, recreated
 * here because the real `common` module/parent project wasn't included in
 * this handoff. Shape is inferred from how it's used in this service
 * (ApiResponse.ok(...), .data()). Replace with the real shared dependency
 * once you have access to the full od-mip-parent repo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
