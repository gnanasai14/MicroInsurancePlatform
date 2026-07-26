package com.odmip.claims.config;

import com.odmip.claims.entity.FraudRule;
import com.odmip.claims.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final FraudRuleRepository fraudRuleRepository;

    @Override
    public void run(String... args) {
        seedFraudRules();
    }

    private void seedFraudRules() {
        if (fraudRuleRepository.count() == 0) {
            log.info("Seeding default fraud rules...");

            FraudRule rule1 = FraudRule.builder()
                    .ruleCode("MULTIPLE_CLAIMS_SHORT_WINDOW")
                    .description("User has submitted 3 or more claims in the last 30 days")
                    .conditionType("HIGH_CLAIM_FREQUENCY")
                    .thresholdCount(3)
                    .riskScoreWeight(45)
                    .active(true)
                    .build();

            FraudRule rule2 = FraudRule.builder()
                    .ruleCode("UNUSUALLY_HIGH_AMOUNT")
                    .description("Claimed amount is greater than 50000")
                    .conditionType("AMOUNT_THRESHOLD")
                    .thresholdAmount(new BigDecimal("50000"))
                    .riskScoreWeight(30)
                    .active(true)
                    .build();

            FraudRule rule3 = FraudRule.builder()
                    .ruleCode("MISSING_DESCRIPTION")
                    .description("Claim has a blank or missing description")
                    .conditionType("MISSING_DESCRIPTION")
                    .riskScoreWeight(10)
                    .active(true)
                    .build();

            fraudRuleRepository.saveAll(List.of(rule1, rule2, rule3));
            log.info("Default fraud rules seeded successfully.");
        } else {
            log.info("Fraud rules already present, skipping seeding.");
        }
    }
}
