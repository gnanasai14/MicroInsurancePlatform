package com.odmip.pricing.dto;

import java.math.BigDecimal;
import java.util.List;

/** Aggregated read-model for the User Dashboard - combines policy + usage + pricing data. */
public record DashboardSummary(
        Long userId,
        int activePolicyCount,
        int totalPolicyCount,
        BigDecimal totalPremiumPaid,
        List<PolicyUsageEntry> policyUsage
) {
    public record PolicyUsageEntry(Long policyId, String policyNumber, String status, Double totalUsage) {}
}
