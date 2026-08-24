package com.odmip.pricing.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.pricing.entity.PricingRule;
import com.odmip.pricing.repository.PricingRuleRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing/rules")
@Tag(name = "Pricing Rules", description = "CRUD operations for Dynamic Pricing Rules")
public class PricingRuleController {

    private final PricingRuleRepository ruleRepository;

    public PricingRuleController(PricingRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public ApiResponse<List<PricingRule>> getAllRules() {
        return ApiResponse.ok(ruleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PricingRule> getRuleById(@PathVariable Long id) {
        PricingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("Rule not found with id " + id));
        return ApiResponse.ok(rule);
    }

    @PostMapping
    @CacheEvict(value = "pricingRules", allEntries = true)
    public ApiResponse<PricingRule> createRule(@Valid @RequestBody PricingRule rule) {
        return ApiResponse.ok("Pricing rule created", ruleRepository.save(rule));
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "pricingRules", allEntries = true)
    public ApiResponse<PricingRule> updateRule(@PathVariable Long id, @Valid @RequestBody PricingRule updatedRule) {
        PricingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("Rule not found with id " + id));
        rule.setType(updatedRule.getType());
        rule.setMatchValue(updatedRule.getMatchValue());
        rule.setMultiplier(updatedRule.getMultiplier());
        rule.setActive(updatedRule.isActive());
        return ApiResponse.ok("Pricing rule updated", ruleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "pricingRules", allEntries = true)
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        PricingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("Rule not found with id " + id));
        ruleRepository.delete(rule);
        return ApiResponse.ok("Pricing rule deleted", null);
    }
}
