package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.common.dto.PolicyDTO;
import com.odmip.user.dto.PolicyCreateRequest;
import com.odmip.user.dto.PolicyPatchRequest;
import com.odmip.user.entity.Policy;
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
    public ApiResponse<PolicyDTO> byId(@PathVariable Long id) {
        Policy policy = policyService.getById(id);
        return ApiResponse.ok(policyService.mapToDTO(policy));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<PolicyDTO>> byUser(@PathVariable Long userId) {
        List<Policy> policies = policyService.findByUser(userId);
        List<PolicyDTO> dtos = policies.stream()
                .map(policyService::mapToDTO)
                .toList();
        return ApiResponse.ok(dtos);
    }

    @PatchMapping("/{id}")
    public ApiResponse<PolicyDTO> patch(@PathVariable Long id, @RequestBody PolicyPatchRequest request) {
        Policy updatedPolicy = policyService.patch(id, request);
        return ApiResponse.ok("Policy updated successfully", policyService.mapToDTO(updatedPolicy));
    }
}
