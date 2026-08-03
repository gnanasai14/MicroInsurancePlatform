package com.odmip.pricing.dto;

public record CouponValidationResponse(
        boolean valid,
        String message
) {}
