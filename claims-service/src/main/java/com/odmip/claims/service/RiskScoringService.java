package com.odmip.claims.service;

import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.RiskScore;
import com.odmip.claims.repository.RiskScoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Risk Scoring System - simple weighted heuristic for week 1/2.
 * Week 3+: swap in a real model / more rules once historical claim data exists.
 */
@Service
public class RiskScoringService {

    private final RiskScoreRepository riskScoreRepository;

    public RiskScoringService(RiskScoreRepository riskScoreRepository) {
        this.riskScoreRepository = riskScoreRepository;
    }

    public RiskScore score(Claim claim, int recentClaimCountForUser) {
        int score = 0;

        // Larger claims score higher risk
        if (claim.getClaimedAmount().compareTo(new BigDecimal("10000")) > 0) score += 30;
        else if (claim.getClaimedAmount().compareTo(new BigDecimal("1000")) > 0) score += 10;

        // Repeat claims in a short window score higher risk
        score += Math.min(recentClaimCountForUser * 15, 45);

        // Missing/very short description is a soft signal
        if (claim.getDescription() == null || claim.getDescription().length() < 15) score += 10;

        score = Math.min(score, 100);

        String tier = score >= 75 ? "CRITICAL" : score >= 50 ? "HIGH" : score >= 25 ? "MEDIUM" : "LOW";

        RiskScore riskScore = RiskScore.builder()
                .claimId(claim.getId())
                .score(score)
                .tier(tier)
                .build();

        return riskScoreRepository.save(riskScore);
    }
}
