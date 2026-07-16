package com.odmip.pricing.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.pricing.dto.UsageRequest;
import com.odmip.pricing.entity.UsageLog;
import com.odmip.pricing.service.UsageTrackingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
@Tag(name = "Usage", description = "Real-time usage tracking + utilization analytics")
public class UsageController {

    private final UsageTrackingService usageTrackingService;

    public UsageController(UsageTrackingService usageTrackingService) {
        this.usageTrackingService = usageTrackingService;
    }

    @PostMapping
    public ApiResponse<UsageLog> record(@Valid @RequestBody UsageRequest request) {
        return ApiResponse.ok("Usage recorded", usageTrackingService.record(request));
    }

    @GetMapping("/policy/{policyId}")
    public ApiResponse<List<UsageLog>> forPolicy(@PathVariable Long policyId) {
        return ApiResponse.ok(usageTrackingService.forPolicy(policyId));
    }

    @GetMapping("/policy/{policyId}/total")
    public ApiResponse<Map<String, Object>> totalForPolicy(@PathVariable Long policyId) {
        return ApiResponse.ok(Map.of("policyId", policyId, "totalUsage", usageTrackingService.totalUsage(policyId)));
    }
}
