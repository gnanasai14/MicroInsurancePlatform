package com.odmip.claims.service;

import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.entity.FraudRule;
import com.odmip.claims.repository.FraudFlagRepository;
import com.odmip.claims.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final FraudFlagRepository fraudFlagRepository;
    private final FraudRuleRepository fraudRuleRepository;

    public List<FraudFlag> evaluate(Claim claim, int recentClaimCountForUser) {
        List<FraudFlag> flags = new ArrayList<>();

        // Evaluate DB-driven dynamic FraudRule entities
        List<FraudRule> dbRules = fraudRuleRepository.findByActiveTrue();
        for (FraudRule rule : dbRules) {
            if ("AMOUNT_THRESHOLD".equalsIgnoreCase(rule.getConditionType())) {
                if (rule.getThresholdAmount() != null && claim.getClaimedAmount().compareTo(rule.getThresholdAmount()) > 0) {
                    flags.add(flag(claim, rule.getRuleCode(), 
                            rule.getDescription() != null ? rule.getDescription() : "Claimed amount exceeds threshold of " + rule.getThresholdAmount()));
                }
            } else if ("HIGH_CLAIM_FREQUENCY".equalsIgnoreCase(rule.getConditionType())) {
                if (rule.getThresholdCount() != null && recentClaimCountForUser >= rule.getThresholdCount()) {
                    flags.add(flag(claim, rule.getRuleCode(), 
                            rule.getDescription() != null ? rule.getDescription() : "User has submitted " + recentClaimCountForUser + " claims in the last 30 days"));
                }
            } else if ("MISSING_DESCRIPTION".equalsIgnoreCase(rule.getConditionType())) {
                if (claim.getDescription() == null || claim.getDescription().trim().isEmpty()) {
                    flags.add(flag(claim, rule.getRuleCode(), 
                            rule.getDescription() != null ? rule.getDescription() : "No description provided for the claim"));
                }
            }
        }

        if (!flags.isEmpty()) {
            fraudFlagRepository.saveAll(flags);
        }
        return flags;
    }

    private FraudFlag flag(Claim claim, String rule, String reason) {
        return FraudFlag.builder().claimId(claim.getId()).ruleTriggered(rule).reason(reason).build();
    }
}
