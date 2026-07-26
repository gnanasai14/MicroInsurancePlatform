package com.odmip.claims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimsAnalyticsDTO {
    private long totalClaimsSubmitted;
    private long totalApprovedClaims;
    private long totalFlaggedFraudClaims;
    private double autoApprovalRatePercentage;
    private BigDecimal totalPayoutAmount;
    private Map<String, Long> claimsByRiskTier;
    private Map<String, Long> flaggedRulesBreakdown;
}
