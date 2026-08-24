package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.user.client.PricingRuleServiceClient;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.User;
import com.odmip.user.repository.UserRepository;
import com.odmip.user.service.PolicyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Panel (Week 2 scope: read/manage endpoints).
 * Locked to ROLE_ADMIN in SecurityConfig ("/api/admin/**").
 * Week 3+: pricing-rule management wired in via PricingRuleServiceClient.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin-only: manage users, policies & pricing rules")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final PolicyService policyService;
    private final PricingRuleServiceClient pricingRuleServiceClient;

    public AdminController(UserRepository userRepository, PolicyService policyService,
                           PricingRuleServiceClient pricingRuleServiceClient) {
        this.userRepository = userRepository;
        this.policyService = policyService;
        this.pricingRuleServiceClient = pricingRuleServiceClient;
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> allUsers() {
        return ApiResponse.ok(userRepository.findAll());
    }

    @GetMapping("/policies")
    public ApiResponse<List<Policy>> allPolicies() {
        return ApiResponse.ok(policyService.findAll());
    }

    @PostMapping("/users/{id}/disable")
    public ApiResponse<User> disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("No user " + id));
        user.setEnabled(false);
        return ApiResponse.ok("User disabled", userRepository.save(user));
    }

    @GetMapping("/pricing-rules")
    public ApiResponse<List<Map<String, Object>>> getPricingRules() {
        return ApiResponse.ok(pricingRuleServiceClient.getAllRules());
    }

    @GetMapping("/pricing-rules/{id}")
    public ApiResponse<Map<String, Object>> getPricingRule(@PathVariable Long id) {
        return ApiResponse.ok(pricingRuleServiceClient.getRuleById(id));
    }

    @PostMapping("/pricing-rules")
    public ApiResponse<Map<String, Object>> createPricingRule(@RequestBody Map<String, Object> rule) {
        return ApiResponse.ok("Pricing rule created successfully", pricingRuleServiceClient.createRule(rule));
    }

    @PutMapping("/pricing-rules/{id}")
    public ApiResponse<Map<String, Object>> updatePricingRule(@PathVariable Long id, @RequestBody Map<String, Object> rule) {
        return ApiResponse.ok("Pricing rule updated successfully", pricingRuleServiceClient.updateRule(id, rule));
    }

    @DeleteMapping("/pricing-rules/{id}")
    public ApiResponse<Void> deletePricingRule(@PathVariable Long id) {
        pricingRuleServiceClient.deleteRule(id);
        return ApiResponse.ok("Pricing rule deleted successfully", null);
    }
}