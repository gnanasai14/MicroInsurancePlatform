package com.odmip.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

public record PremiumQuoteResponse(
        BigDecimal basePremium,
        List<String> appliedRules,
        BigDecimal multiplierApplied,
        BigDecimal premiumBeforeDiscount,
        BigDecimal discountApplied,
        BigDecimal finalPremium
) {}
