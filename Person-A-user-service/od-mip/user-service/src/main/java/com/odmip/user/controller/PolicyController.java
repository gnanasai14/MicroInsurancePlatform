package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.user.dto.PolicyCreateRequest;
import com.odmip.user.dto.PolicyPatchRequest;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyPremiumHistory;
import com.odmip.user.service.PolicyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@Tag(name = "Policies", description = "On-demand policy creation, activation, lookup")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ApiResponse<Policy> create(@Valid @RequestBody PolicyCreateRequest request) {
        return ApiResponse.ok("Policy created (DRAFT)", policyService.create(request));
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<Policy> activate(@PathVariable Long id) {
        return ApiResponse.ok("Policy activated", policyService.activate(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Policy> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Policy cancelled", policyService.cancel(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<Policy> byId(@PathVariable Long id) {
        return ApiResponse.ok(policyService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Policy>> byUser(@PathVariable Long userId) {
        return ApiResponse.ok(policyService.findByUser(userId));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Policy> patch(@PathVariable Long id, @Valid @RequestBody PolicyPatchRequest request) {
        return ApiResponse.ok("Policy updated", policyService.patchPolicy(id, request));
    }

    @GetMapping("/{id}/premium-history")
    public ApiResponse<List<PolicyPremiumHistory>> premiumHistory(@PathVariable Long id) {
        return ApiResponse.ok(policyService.getPremiumHistory(id));
    }
}
