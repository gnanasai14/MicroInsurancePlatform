package com.odmip.claims.service;

import com.odmip.claims.dto.ClaimsAnalyticsDTO;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.repository.ClaimRepository;
import com.odmip.claims.repository.ClaimSpecification;
import com.odmip.claims.repository.FraudFlagRepository;
import com.odmip.claims.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public ClaimsAnalyticsDTO getAnalytics(ClaimStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        Specification<Claim> spec = Specification.where(ClaimSpecification.hasStatus(status))
                .and(ClaimSpecification.submittedAfter(startDate))
                .and(ClaimSpecification.submittedBefore(endDate));

        List<Claim> allClaims = claimRepository.findAll(spec);
        long totalSubmitted = allClaims.size();
        long approvedCount = allClaims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED).count();
        
        Set<Long> claimIds = allClaims.stream().map(Claim::getId).collect(Collectors.toSet());

        List<FraudFlag> allFlags = fraudFlagRepository.findAll().stream()
                .filter(f -> claimIds.contains(f.getClaimId()))
                .toList();

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
                .filter(r -> claimIds.contains(r.getClaimId()))
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
