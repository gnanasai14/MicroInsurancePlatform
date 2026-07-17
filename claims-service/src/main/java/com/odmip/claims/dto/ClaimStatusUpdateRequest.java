package com.odmip.claims.dto;

import jakarta.validation.constraints.NotNull;

public record ClaimStatusUpdateRequest(
        @NotNull String newStatus,
        String note
) {}
