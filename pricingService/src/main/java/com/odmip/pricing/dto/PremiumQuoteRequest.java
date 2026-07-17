package com.odmip.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Ad-hoc quote request - doesn't require a policy to already exist. */
public record PremiumQuoteRequest(
        @NotNull @Positive BigDecimal basePremium,
        @NotBlank String riskCategory,     // LOW, MEDIUM, HIGH
        String location,                   // URBAN, RURAL, etc (optional)
        String usageLevel,                 // LIGHT, MODERATE, HEAVY (optional)
        @NotNull @Positive Integer durationHours,
        String couponCode                  // optional
) {}
