package com.odmip.claims.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.claims.dto.ClaimStatusUpdateRequest;
import com.odmip.claims.dto.ClaimSubmitRequest;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import com.odmip.claims.service.ClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Claims", description = "Submission, validation, status tracking")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ApiResponse<Claim> submit(@Valid @RequestBody ClaimSubmitRequest request) {
        return ApiResponse.ok("Claim submitted", claimService.submit(request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Claim> updateStatus(@PathVariable Long id, @Valid @RequestBody ClaimStatusUpdateRequest request) {
        ClaimStatus newStatus = ClaimStatus.valueOf(request.newStatus().toUpperCase());
        return ApiResponse.ok("Status updated", claimService.updateStatus(id, newStatus, request.note()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Claim> byId(@PathVariable Long id) {
        return ApiResponse.ok(claimService.getById(id));
    }

    @GetMapping("/policy/{policyId}")
    public ApiResponse<List<Claim>> byPolicy(@PathVariable Long policyId) {
        return ApiResponse.ok(claimService.findByPolicy(policyId));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Claim>> byUser(@PathVariable Long userId) {
        return ApiResponse.ok(claimService.findByUser(userId));
    }
}
