package com.odmip.pricing.config;

import com.odmip.pricing.entity.PricingRule;
import com.odmip.pricing.entity.RuleType;
import com.odmip.pricing.repository.PricingRuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Seeds a starter rule-set so the pricing engine has something to apply from day 1. */
@Component
public class RuleSeeder implements CommandLineRunner {

    private final PricingRuleRepository ruleRepository;

    public RuleSeeder(PricingRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void run(String... args) {
        if (ruleRepository.count() > 0) return;

        ruleRepository.saveAll(java.util.List.of(
                rule(RuleType.RISK, "LOW", "1.00"),
                rule(RuleType.RISK, "MEDIUM", "1.25"),
                rule(RuleType.RISK, "HIGH", "1.60"),
                rule(RuleType.LOCATION, "URBAN", "1.10"),
                rule(RuleType.LOCATION, "RURAL", "0.95"),
                rule(RuleType.USAGE, "LIGHT", "0.90"),
                rule(RuleType.USAGE, "MODERATE", "1.00"),
                rule(RuleType.USAGE, "HEAVY", "1.30")
        ));
    }

    private PricingRule rule(RuleType type, String value, String multiplier) {
        return PricingRule.builder()
                .type(type)
                .matchValue(value)
                .multiplier(new BigDecimal(multiplier))
                .active(true)
                .build();
    }
}
