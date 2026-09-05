package com.odmip.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

public record PremiumQuoteResponse(
        Long quoteId,
        String status,          // PENDING - nothing is committed until /accept is called
        BigDecimal basePremium,
        List<String> appliedRules,
        BigDecimal multiplierApplied,
        BigDecimal premiumBeforeDiscount,
        BigDecimal discountApplied,
        BigDecimal finalPremium
) {}
