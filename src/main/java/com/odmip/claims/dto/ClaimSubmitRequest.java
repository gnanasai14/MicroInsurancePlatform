package com.odmip.claims.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ClaimSubmitRequest(
        @NotNull Long policyId,
        @NotNull Long userId,
        @NotNull @Positive BigDecimal claimedAmount,
        @Size(max = 1000) String description
) {}
