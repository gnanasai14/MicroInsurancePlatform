package com.odmip.claims.controller;

import com.odmip.claims.dto.ClaimsAnalyticsDTO;
import com.odmip.claims.dto.ClaimStatusUpdateRequest;
import com.odmip.claims.dto.ClaimSubmitRequest;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import com.odmip.claims.service.ClaimService;
import com.odmip.claims.service.ClaimsAnalyticsService;
import com.odmip.common.dto.ApiResponse;
import com.odmip.common.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Submission, validation, status tracking (Person C)")
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimsAnalyticsService analyticsService;

    @PostMapping
    @Operation(summary = "Submit a new claim")
    public ApiResponse<Claim> submit(@Valid @RequestBody ClaimSubmitRequest request) {
        return ApiResponse.ok("Claim submitted", claimService.submit(request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update claim status")
    public ApiResponse<Claim> updateStatus(@PathVariable Long id, @Valid @RequestBody ClaimStatusUpdateRequest request) {
        ClaimStatus newStatus;
        try {
            newStatus = ClaimStatus.valueOf(request.newStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid status '" + request.newStatus() + "'. Valid values: "
                    + java.util.Arrays.toString(ClaimStatus.values()));
        }
        return ApiResponse.ok("Status updated", claimService.updateStatus(id, newStatus, request.note()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim by ID")
    public ApiResponse<Claim> byId(@PathVariable Long id) {
        return ApiResponse.ok(claimService.getById(id));
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get claims for policy")
    public ApiResponse<List<Claim>> byPolicy(@PathVariable Long policyId) {
        return ApiResponse.ok(claimService.findByPolicy(policyId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get claims for user")
    public ApiResponse<List<Claim>> byUser(@PathVariable Long userId) {
        return ApiResponse.ok(claimService.findByUser(userId));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get claims & fraud analytics dashboard metrics")
    public ApiResponse<ClaimsAnalyticsDTO> getAnalytics(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate
    ) {
        return ApiResponse.ok(analyticsService.getAnalytics(status, startDate, endDate));
    }

    @GetMapping("/{id}/notifications")
    @Operation(summary = "Get notification delivery audits for a specific claim")
    public ApiResponse<List<com.odmip.claims.entity.NotificationAudit>> getClaimNotifications(@PathVariable Long id) {
        return ApiResponse.ok(claimService.getNotificationAudits(id));
    }
}
