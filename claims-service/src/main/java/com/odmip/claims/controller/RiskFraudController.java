package com.odmip.claims.controller;

import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.entity.FraudRule;
import com.odmip.claims.entity.RiskScore;
import com.odmip.claims.repository.FraudFlagRepository;
import com.odmip.claims.repository.FraudRuleRepository;
import com.odmip.claims.repository.RiskScoreRepository;
import com.odmip.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
@Tag(name = "Risk & Fraud", description = "Risk scores, fraud flags, and dynamic fraud rules (Person C)")
public class RiskFraudController {

    private final RiskScoreRepository riskScoreRepository;
    private final FraudFlagRepository fraudFlagRepository;
    private final FraudRuleRepository fraudRuleRepository;

    @GetMapping("/claims/{claimId}/score")
    @Operation(summary = "Get risk score for a claim")
    public ApiResponse<RiskScore> score(@PathVariable Long claimId) {
        return ApiResponse.ok(riskScoreRepository.findByClaimId(claimId)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException(
                        "No risk score for claim " + claimId)));
    }

    @GetMapping("/claims/{claimId}/flags")
    @Operation(summary = "Get fraud flags for a claim")
    public ApiResponse<List<FraudFlag>> flags(@PathVariable Long claimId) {
        return ApiResponse.ok(fraudFlagRepository.findByClaimId(claimId));
    }

    @PostMapping("/rules")
    @Operation(summary = "Add or update dynamic database-configurable fraud rule")
    public ApiResponse<FraudRule> addOrUpdateRule(@RequestBody FraudRule rule) {
        java.util.Optional<FraudRule> existing = fraudRuleRepository.findByRuleCode(rule.getRuleCode());
        if (existing.isPresent()) {
            FraudRule toUpdate = existing.get();
            toUpdate.setDescription(rule.getDescription());
            toUpdate.setConditionType(rule.getConditionType());
            toUpdate.setThresholdAmount(rule.getThresholdAmount());
            toUpdate.setThresholdCount(rule.getThresholdCount());
            toUpdate.setRiskScoreWeight(rule.getRiskScoreWeight());
            toUpdate.setActive(rule.isActive());
            return ApiResponse.ok("Rule updated successfully", fraudRuleRepository.save(toUpdate));
        } else {
            return ApiResponse.ok("Rule created successfully", fraudRuleRepository.save(rule));
        }
    }

    @GetMapping("/rules")
    @Operation(summary = "Get active dynamic fraud rules")
    public ApiResponse<List<FraudRule>> getRules() {
        return ApiResponse.ok(fraudRuleRepository.findByActiveTrue());
    }

    @GetMapping("/rules/{id}")
    @Operation(summary = "Get a dynamic fraud rule by ID")
    public ApiResponse<FraudRule> getRuleById(@PathVariable Long id) {
        return ApiResponse.ok(fraudRuleRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException(
                        "No fraud rule with id " + id)));
    }
}
