package com.odmip.pricing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PremiumHistoryDTO(
        Long id,
        Long policyId,
        BigDecimal premiumAmount,
        LocalDateTime changedAt
) {}
