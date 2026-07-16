package com.odmip.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UsageRequest(
        @NotNull Long policyId,
        @NotNull Long userId,
        @NotBlank String usageType,
        @NotNull @Positive Double quantity
) {}
