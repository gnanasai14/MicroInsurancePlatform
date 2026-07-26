package com.odmip.claims.service;

import com.odmip.claims.dto.ClaimsAnalyticsDTO;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.repository.ClaimRepository;
import com.odmip.claims.repository.FraudFlagRepository;
import com.odmip.claims.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimsAnalyticsService {

    private final ClaimRepository claimRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final RiskScoreRepository riskScoreRepository;

    public ClaimsAnalyticsDTO getAnalytics() {
        List<Claim> allClaims = claimRepository.findAll();
        long totalSubmitted = allClaims.size();
        long approvedCount = allClaims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED).count();
        
        List<FraudFlag> allFlags = fraudFlagRepository.findAll();
        long flaggedCount = allFlags.stream().map(FraudFlag::getClaimId).distinct().count();
        Set<Long> flaggedClaimIds = allFlags.stream().map(FraudFlag::getClaimId).collect(Collectors.toSet());

        // Auto-approved/validated: progressed past SUBMITTED without triggering any fraud flags
        long autoApprovedCount = allClaims.stream()
                .filter(c -> !flaggedClaimIds.contains(c.getId()))
                .filter(c -> c.getStatus() == ClaimStatus.VALIDATED || c.getStatus() == ClaimStatus.UNDER_REVIEW || c.getStatus() == ClaimStatus.APPROVED)
                .count();

        BigDecimal totalPayout = allClaims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
                .map(Claim::getClaimedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double autoApprovalRate = totalSubmitted > 0 ? ((double) autoApprovedCount / totalSubmitted) * 100.0 : 0.0;

        Map<String, Long> riskTiers = riskScoreRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getTier() != null ? r.getTier() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> rulesBreakdown = allFlags.stream()
                .collect(Collectors.groupingBy(FraudFlag::getRuleTriggered, Collectors.counting()));

        return ClaimsAnalyticsDTO.builder()
                .totalClaimsSubmitted(totalSubmitted)
                .totalApprovedClaims(approvedCount)
                .totalFlaggedFraudClaims(flaggedCount)
                .autoApprovalRatePercentage(Math.round(autoApprovalRate * 100.0) / 100.0)
                .totalPayoutAmount(totalPayout)
                .claimsByRiskTier(riskTiers)
                .flaggedRulesBreakdown(rulesBreakdown)
                .build();
    }
}
