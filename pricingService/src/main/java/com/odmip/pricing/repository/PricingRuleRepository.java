package com.odmip.pricing.repository;

import com.odmip.pricing.entity.PricingRule;
import com.odmip.pricing.entity.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByTypeAndActiveTrue(RuleType type);
}
