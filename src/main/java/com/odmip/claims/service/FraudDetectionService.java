package com.odmip.claims.service;

import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.repository.FraudFlagRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fraud Detection Rules - deterministic, explainable rules (not ML) for
 * week 1/2. Each triggered rule produces a FraudFlag row.
 */
@Service
public class FraudDetectionService {

    private final FraudFlagRepository fraudFlagRepository;

    public FraudDetectionService(FraudFlagRepository fraudFlagRepository) {
        this.fraudFlagRepository = fraudFlagRepository;
    }

    public List<FraudFlag> evaluate(Claim claim, int recentClaimCountForUser) {
        List<FraudFlag> flags = new ArrayList<>();

        if (recentClaimCountForUser >= 3) {
            flags.add(flag(claim, "MULTIPLE_CLAIMS_SHORT_WINDOW",
                    "User has submitted " + recentClaimCountForUser + " claims in the last 30 days"));
        }

        if (claim.getClaimedAmount().compareTo(new BigDecimal("50000")) > 0) {
            flags.add(flag(claim, "UNUSUALLY_HIGH_AMOUNT",
                    "Claimed amount " + claim.getClaimedAmount() + " is unusually high"));
        }

        if (claim.getDescription() == null || claim.getDescription().isBlank()) {
            flags.add(flag(claim, "MISSING_DESCRIPTION", "No description provided for the claim"));
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
