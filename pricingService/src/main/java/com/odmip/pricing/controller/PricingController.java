package com.odmip.pricing.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.pricing.dto.PremiumQuoteRequest;
import com.odmip.pricing.dto.PremiumQuoteResponse;
import com.odmip.pricing.service.PremiumCalculatorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@Tag(name = "Pricing", description = "Time-based premium calculation + dynamic pricing engine")
public class PricingController {

    private final PremiumCalculatorService calculatorService;

    public PricingController(PremiumCalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/quote")
    public ApiResponse<PremiumQuoteResponse> quote(@Valid @RequestBody PremiumQuoteRequest request) {
        return ApiResponse.ok(calculatorService.quote(request));
    }
}
