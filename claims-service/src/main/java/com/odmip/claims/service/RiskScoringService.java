package com.odmip.claims.service;

import com.odmip.claims.client.PolicyServiceClient;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.entity.FraudRule;
import com.odmip.claims.entity.RiskScore;
import com.odmip.claims.repository.ClaimRepository;
import com.odmip.claims.repository.FraudFlagRepository;
import com.odmip.claims.repository.RiskScoreRepository;
import com.odmip.claims.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoringService {

    private final RiskScoreRepository riskScoreRepository;
    private final PolicyServiceClient policyServiceClient;
    private final FraudRuleRepository fraudRuleRepository;
    private final ClaimRepository claimRepository;
    private final FraudFlagRepository fraudFlagRepository;

    public RiskScore score(Claim claim, int recentClaimCountForUser, List<FraudFlag> flags) {
        int score = 0;

        // 1. User claim history frequency (recent claims in last 30 days)
        score += Math.min(recentClaimCountForUser * 15, 45);

        // 2. Claim-to-coverage ratio
        BigDecimal coverageLimit = BigDecimal.ZERO;
        try {
            Map<String, Object> policy = policyServiceClient.getPolicy(claim.getPolicyId());
            if (policy != null && policy.get("coverageAmount") != null) {
                coverageLimit = new BigDecimal(policy.get("coverageAmount").toString());
            }
        } catch (Exception ex) {
            log.warn("Could not retrieve policy coverage limit for claim {}: {}. Proceeding without coverage ratio.", claim.getId(), ex.getMessage());
        }

        if (coverageLimit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = claim.getClaimedAmount().divide(coverageLimit, 4, RoundingMode.HALF_UP);
            log.info("Claim {} claimed amount: {}, coverage limit: {}, ratio: {}", claim.getId(), claim.getClaimedAmount(), coverageLimit, ratio);
            if (ratio.compareTo(BigDecimal.ONE) > 0) {
                score += 50; // Claim exceeds policy coverage amount
            } else if (ratio.compareTo(new BigDecimal("0.8")) > 0) {
                score += 40;
            } else if (ratio.compareTo(new BigDecimal("0.5")) > 0) {
                score += 25;
            } else if (ratio.compareTo(new BigDecimal("0.2")) > 0) {
                score += 10;
            }
        } else {
            // Default amount-based logic fallback if coverage limit isn't available
            if (claim.getClaimedAmount().compareTo(new BigDecimal("10000")) > 0) {
                score += 30;
            } else if (claim.getClaimedAmount().compareTo(new BigDecimal("1000")) > 0) {
                score += 10;
            }
        }

        // 3. Cumulative fraud score
        // A. Sum of risk weights of triggered rules for the current claim
        List<FraudRule> rules = fraudRuleRepository.findByActiveTrue();
        int currentClaimFraudWeight = 0;
        for (FraudFlag flag : flags) {
            String ruleCode = flag.getRuleTriggered();
            Optional<FraudRule> ruleOpt = rules.stream().filter(r -> r.getRuleCode().equalsIgnoreCase(ruleCode)).findFirst();
            if (ruleOpt.isPresent()) {
                currentClaimFraudWeight += ruleOpt.get().getRiskScoreWeight();
            } else {
                // Fallbacks
                if ("MULTIPLE_CLAIMS_SHORT_WINDOW".equalsIgnoreCase(ruleCode)) {
                    currentClaimFraudWeight += 45;
                } else if ("UNUSUALLY_HIGH_AMOUNT".equalsIgnoreCase(ruleCode)) {
                    currentClaimFraudWeight += 30;
                } else if ("MISSING_DESCRIPTION".equalsIgnoreCase(ruleCode)) {
                    currentClaimFraudWeight += 10;
                }
            }
        }
        score += currentClaimFraudWeight;

        // B. Plus user's historical fraud flags count
        try {
            List<Long> historicalClaimIds = claimRepository.findByUserId(claim.getUserId()).stream()
                    .map(Claim::getId)
                    .filter(id -> !id.equals(claim.getId()))
                    .toList();
            if (!historicalClaimIds.isEmpty()) {
                long historicalFlagsCount = fraudFlagRepository.countByClaimIdIn(historicalClaimIds);
                score += Math.min(historicalFlagsCount * 10, 30);
            }
        } catch (Exception ex) {
            log.warn("Could not calculate user historical fraud score: {}", ex.getMessage());
        }

        // Cap risk score between 0 and 100
        score = Math.max(0, Math.min(score, 100));

        String tier = score >= 75 ? "CRITICAL" : score >= 50 ? "HIGH" : score >= 25 ? "MEDIUM" : "LOW";

        RiskScore riskScore = RiskScore.builder()
                .claimId(claim.getId())
                .score(score)
                .tier(tier)
                .build();

        return riskScoreRepository.save(riskScore);
    }
}
