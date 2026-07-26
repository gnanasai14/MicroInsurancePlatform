package com.odmip.pricing.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank String code,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal discountPercent,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validUntil,
        @NotNull @Positive Integer maxRedemptions,
        Integer maxRedemptionsPerUser
) {}
