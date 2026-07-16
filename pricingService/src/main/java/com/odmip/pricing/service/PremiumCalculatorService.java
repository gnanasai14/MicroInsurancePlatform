package com.odmip.pricing.service;

import com.odmip.pricing.dto.PremiumQuoteRequest;
import com.odmip.pricing.dto.PremiumQuoteResponse;
import com.odmip.pricing.entity.PricingRule;
import com.odmip.pricing.entity.RuleType;
import com.odmip.pricing.repository.PricingRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Time-Based Premium Calculation + Dynamic Pricing Engine, combined:
 *  1. Start from the template's base premium.
 *  2. Apply a time factor (premium scales with coverage duration).
 *  3. Apply risk / location / usage multipliers (Dynamic Pricing Engine).
 *  4. Apply a coupon discount, if any (delegated to CouponService).
 */
@Service
public class PremiumCalculatorService {

    private static final BigDecimal HOURS_PER_DAY = new BigDecimal("24");

    private final PricingRuleRepository ruleRepository;
    private final CouponService couponService;

    public PremiumCalculatorService(PricingRuleRepository ruleRepository, CouponService couponService) {
        this.ruleRepository = ruleRepository;
        this.couponService = couponService;
    }

    public PremiumQuoteResponse quote(PremiumQuoteRequest req) {
        List<String> appliedRules = new ArrayList<>();

        // Step 1: time-based factor - >1 day scales premium proportionally, min 1 day charged
        BigDecimal days = BigDecimal.valueOf(req.durationHours())
                .divide(HOURS_PER_DAY, 4, RoundingMode.HALF_UP);
        BigDecimal timeFactor = days.max(BigDecimal.ONE);

        // Step 2: dynamic multipliers
        BigDecimal multiplier = BigDecimal.ONE;
        multiplier = multiplier.multiply(findMultiplier(RuleType.RISK, req.riskCategory(), appliedRules));
        if (req.location() != null) {
            multiplier = multiplier.multiply(findMultiplier(RuleType.LOCATION, req.location(), appliedRules));
        }
        if (req.usageLevel() != null) {
            multiplier = multiplier.multiply(findMultiplier(RuleType.USAGE, req.usageLevel(), appliedRules));
        }

        BigDecimal premiumBeforeDiscount = req.basePremium()
                .multiply(timeFactor)
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // Step 3: coupon
        BigDecimal discount = BigDecimal.ZERO;
        if (req.couponCode() != null && !req.couponCode().isBlank()) {
            discount = couponService.calculateDiscount(req.couponCode(), premiumBeforeDiscount);
        }

        BigDecimal finalPremium = premiumBeforeDiscount.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        if (finalPremium.compareTo(BigDecimal.ZERO) < 0) finalPremium = BigDecimal.ZERO;

        return new PremiumQuoteResponse(
                req.basePremium(), appliedRules, multiplier, premiumBeforeDiscount, discount, finalPremium);
    }

    private BigDecimal findMultiplier(RuleType type, String value, List<String> appliedRules) {
        Optional<PricingRule> rule = ruleRepository.findByTypeAndActiveTrue(type).stream()
                .filter(r -> r.getMatchValue().equalsIgnoreCase(value))
                .findFirst();

        if (rule.isPresent()) {
            appliedRules.add(type + ":" + value + " x" + rule.get().getMultiplier());
            return rule.get().getMultiplier();
        }
        return BigDecimal.ONE; // no matching rule -> neutral
    }
}
