package com.odmip.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Shared, read-only "wire contract" for a Policy.
 * Owned by Person A (user-service / policy-service).
 * Person B (pricing) and Person C (claims) should depend on THIS shape,
 * not reach into policy-service's internal entity.
 */
public record PolicyDTO(
        Long id,
        Long userId,
        String policyNumber,
        String templateCode,
        String status,          // DRAFT, ACTIVE, EXPIRED, CANCELLED
        BigDecimal coverageAmount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String riskCategory      // used by pricing + risk scoring
) {}
