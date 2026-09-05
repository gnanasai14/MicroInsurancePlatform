package com.odmip.pricing.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.pricing.client.PolicyServiceClient;
import com.odmip.pricing.dto.PremiumQuoteRequest;
import com.odmip.pricing.dto.PremiumQuoteResponse;
import com.odmip.pricing.entity.PricingRule;
import com.odmip.pricing.entity.RuleType;
import com.odmip.pricing.entity.Quote;
import com.odmip.pricing.entity.QuoteStatus;
import com.odmip.pricing.repository.PricingRuleRepository;
import com.odmip.pricing.repository.QuoteRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Time-Based Premium Calculation + Dynamic Pricing Engine, combined:
 *  1. Start from the template's base premium.
 *  2. Apply a time factor (premium scales with coverage duration).
 *  3. Apply risk / location / usage multipliers (Dynamic Pricing Engine).
 *  4. Apply time-of-day surge pricing multipliers.
 *  5. Apply coupon discounts (delegated to CouponService, supports stacking & limits).
 *  6. Persist as a PENDING quote - nothing is pushed to the policy or emailed
 *     yet. That only happens once the customer explicitly accepts (pays for)
 *     the quote via acceptQuote() - see that method for why this changed
 *     from the original "push immediately" behavior.
 */
@Service
public class PremiumCalculatorService {

    private static final BigDecimal HOURS_PER_DAY = new BigDecimal("24");

    private final PricingRuleRepository ruleRepository;
    private final CouponService couponService;
    private final QuoteRepository quoteRepository;
    private final PolicyServiceClient policyServiceClient;
    private final EmailNotificationService emailNotificationService;

    public PremiumCalculatorService(PricingRuleRepository ruleRepository,
                                    CouponService couponService,
                                    QuoteRepository quoteRepository,
                                    PolicyServiceClient policyServiceClient,
                                    EmailNotificationService emailNotificationService) {
        this.ruleRepository = ruleRepository;
        this.couponService = couponService;
        this.quoteRepository = quoteRepository;
        this.policyServiceClient = policyServiceClient;
        this.emailNotificationService = emailNotificationService;
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

        // Step 3: Surge check (Time-of-day)
        BigDecimal surgeMultiplier = BigDecimal.ONE;
        List<PricingRule> surgeRules = ruleRepository.findByTypeAndActiveTrue(RuleType.SURGE);
        LocalTime now = LocalTime.now();
        for (PricingRule rule : surgeRules) {
            String[] parts = rule.getMatchValue().split("-");
            if (parts.length == 2) {
                try {
                    LocalTime start = LocalTime.parse(parts[0].trim());
                    LocalTime end = LocalTime.parse(parts[1].trim());
                    boolean matches;
                    if (start.isBefore(end)) {
                        matches = !now.isBefore(start) && !now.isAfter(end);
                    } else { // Overnight range, e.g. 23:00 to 04:00
                        matches = !now.isBefore(start) || !now.isAfter(end);
                    }
                    if (matches) {
                        appliedRules.add("SURGE:" + rule.getMatchValue() + " x" + rule.getMultiplier());
                        surgeMultiplier = surgeMultiplier.multiply(rule.getMultiplier());
                    }
                } catch (Exception e) {
                    // Ignore format errors
                }
            }
        }
        multiplier = multiplier.multiply(surgeMultiplier);

        BigDecimal premiumBeforeDiscount = req.basePremium()
                .multiply(timeFactor)
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // Step 4: coupon
        BigDecimal discount = BigDecimal.ZERO;
        if (req.couponCode() != null && !req.couponCode().isBlank()) {
            discount = couponService.calculateDiscount(req.couponCode(), premiumBeforeDiscount, req.userId());
        }

        BigDecimal finalPremium = premiumBeforeDiscount.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        if (finalPremium.compareTo(BigDecimal.ZERO) < 0) finalPremium = BigDecimal.ZERO;

        // Step 5: Persist as PENDING - no policy push, no email yet. See acceptQuote().
        Quote quoteRecord = Quote.builder()
                .policyId(req.policyId())
                .userId(req.userId())
                .riskCategory(req.riskCategory())
                .basePremium(req.basePremium())
                .finalPremium(finalPremium)
                .couponCode(req.couponCode())
                .discountAmount(discount)
                .createdAt(LocalDateTime.now())
                .status(QuoteStatus.PENDING)
                .build();
        Quote saved = quoteRepository.save(quoteRecord);

        return new PremiumQuoteResponse(
                saved.getId(), saved.getStatus().name(),
                req.basePremium(), appliedRules, multiplier, premiumBeforeDiscount, discount, finalPremium);
    }

    /**
     * Customer clicked "Pay." This is the moment the quote actually becomes
     * real: the premium is pushed to the policy (populating premium
     * history), the policy is activated if it was still DRAFT, and a
     * confirmation email goes out. Previously all of this happened the
     * instant a quote was generated - before the customer had agreed to
     * anything - which is why premium history never lined up with an actual
     * purchase decision.
     */
    public Quote acceptQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("No quote with id " + quoteId));
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new BusinessRuleException("This quote is already " + quote.getStatus() + " and can't be accepted again.");
        }

        if (quote.getPolicyId() != null) {
            policyServiceClient.updatePolicyPremium(quote.getPolicyId(), quote.getFinalPremium()).block();
            policyServiceClient.activatePolicy(quote.getPolicyId()).block();
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setDecidedAt(LocalDateTime.now());
        Quote saved = quoteRepository.save(quote);

        try {
            com.odmip.common.dto.UserDTO userDto = policyServiceClient.getUser(quote.getUserId()).block();
            if (userDto != null && userDto.email() != null) {
                String policyIdOrNum = quote.getPolicyId() != null ? String.valueOf(quote.getPolicyId()) : "NEW_QUOTE";
                emailNotificationService.sendQuoteConfirmation(userDto.email(), policyIdOrNum, quote.getFinalPremium());
            }
        } catch (Exception ignored) {
            // Non-fatal - the purchase itself already succeeded above.
        }

        return saved;
    }

    /** Customer backed out at the payment step. No side effects beyond marking it cancelled. */
    public Quote cancelQuote(Long quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("No quote with id " + quoteId));
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new BusinessRuleException("This quote is already " + quote.getStatus() + ".");
        }
        quote.setStatus(QuoteStatus.CANCELLED);
        quote.setDecidedAt(LocalDateTime.now());
        return quoteRepository.save(quote);
    }

    /** Admin visibility into every quote a customer has actually paid for - "coverage taken." */
    public List<Quote> getAcceptedQuotes() {
        return quoteRepository.findByStatusOrderByDecidedAtDesc(QuoteStatus.ACCEPTED);
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

    public java.util.Map<String, Object> getAnalyticsSummary() {
        // Revenue by risk category
        java.util.Map<String, BigDecimal> revenueMap = new java.util.HashMap<>();
        for (Object[] row : quoteRepository.aggregateRevenueByRiskCategory()) {
            if (row.length == 2 && row[0] != null && row[1] != null) {
                revenueMap.put(row[0].toString(), (BigDecimal) row[1]);
            }
        }

        // Coupon redemptions
        java.util.Map<String, Integer> couponMap = new java.util.HashMap<>();
        for (com.odmip.pricing.entity.Coupon coupon : couponService.getAllCoupons()) {
            couponMap.put(coupon.getCode(), coupon.getTimesRedeemed());
        }

        return java.util.Map.of(
                "revenueByRiskCategory", revenueMap,
                "couponRedemptions", couponMap
        );
    }
}
