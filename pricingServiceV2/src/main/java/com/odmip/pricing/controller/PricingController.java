package com.odmip.pricing.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.pricing.dto.PremiumQuoteRequest;
import com.odmip.pricing.dto.PremiumQuoteResponse;
import com.odmip.pricing.entity.Quote;
import com.odmip.pricing.service.PremiumCalculatorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /** Customer clicked "Pay" - commits the quote: pushes premium, activates the policy, sends confirmation. */
    @PostMapping("/quote/{id}/accept")
    public ApiResponse<Quote> acceptQuote(@PathVariable Long id) {
        return ApiResponse.ok("Coverage confirmed", calculatorService.acceptQuote(id));
    }

    /** Customer clicked "Cancel" at the payment step - no side effects. */
    @PostMapping("/quote/{id}/cancel")
    public ApiResponse<Quote> cancelQuote(@PathVariable Long id) {
        return ApiResponse.ok("Quote cancelled", calculatorService.cancelQuote(id));
    }

    /** Admin visibility: every quote a customer has actually paid for. */
    @GetMapping("/quote/accepted")
    public ApiResponse<List<Quote>> acceptedQuotes() {
        return ApiResponse.ok(calculatorService.getAcceptedQuotes());
    }

    @GetMapping("/analytics/summary")
    public ApiResponse<java.util.Map<String, Object>> getAnalyticsSummary() {
        return ApiResponse.ok(calculatorService.getAnalyticsSummary());
    }
}
