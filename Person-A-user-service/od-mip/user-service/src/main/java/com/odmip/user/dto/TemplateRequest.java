package com.odmip.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TemplateRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal baseCoverageAmount,
        @NotNull @Positive BigDecimal basePremium,
        @NotNull @Positive Integer defaultDurationHours,
        @NotBlank String riskCategory
) {}
