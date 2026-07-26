package com.odmip.pricing.repository;

import com.odmip.pricing.entity.PricingRule;
import com.odmip.pricing.entity.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    @Cacheable(value = "pricingRules", key = "#type")
    List<PricingRule> findByTypeAndActiveTrue(RuleType type);
}
