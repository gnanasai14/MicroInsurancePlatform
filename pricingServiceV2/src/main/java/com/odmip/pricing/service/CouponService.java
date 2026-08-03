package com.odmip.pricing.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.pricing.dto.CouponRequest;
import com.odmip.pricing.entity.Coupon;
import com.odmip.pricing.entity.CouponRedemption;
import com.odmip.pricing.repository.CouponRepository;
import com.odmip.pricing.repository.CouponRedemptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;

    public CouponService(CouponRepository couponRepository, CouponRedemptionRepository redemptionRepository) {
        this.couponRepository = couponRepository;
        this.redemptionRepository = redemptionRepository;
    }

    public Coupon create(CouponRequest req) {
        if (req.validUntil().isBefore(req.validFrom())) {
            throw new BusinessRuleException("validUntil must be after validFrom");
        }
        Coupon coupon = Coupon.builder()
                .code(req.code().toUpperCase())
                .discountPercent(req.discountPercent())
                .validFrom(req.validFrom())
                .validUntil(req.validUntil())
                .maxRedemptions(req.maxRedemptions())
                .maxRedemptionsPerUser(req.maxRedemptionsPerUser())
                .timesRedeemed(0)
                .active(true)
                .build();
        return couponRepository.save(coupon);
    }

    /** Validates + "redeems" multiple comma-separated coupons and returns the cumulative discount amount. */
    @Transactional
    public BigDecimal calculateDiscount(String codesStr, BigDecimal initialPremium, Long userId) {
        String[] codes = codesStr.split(",");
        BigDecimal currentPremium = initialPremium;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (String c : codes) {
            final String cleanCode = c.trim();
            if (cleanCode.isEmpty()) continue;

            Coupon coupon = couponRepository.findByCodeAndActiveTrue(cleanCode.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("No active coupon: " + cleanCode));

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
                throw new BusinessRuleException("Coupon " + cleanCode + " is not valid right now");
            }
            if (coupon.getTimesRedeemed() >= coupon.getMaxRedemptions()) {
                throw new BusinessRuleException("Coupon " + cleanCode + " has reached its redemption limit");
            }

            // Per-user limit check
            if (userId != null && coupon.getMaxRedemptionsPerUser() != null) {
                int userRedemptions = redemptionRepository.findByCouponIdAndUserId(coupon.getId(), userId)
                        .map(CouponRedemption::getRedemptionCount)
                        .orElse(0);
                if (userRedemptions >= coupon.getMaxRedemptionsPerUser()) {
                    throw new BusinessRuleException("Coupon " + cleanCode + " has reached its redemption limit for this user");
                }
            }

            // Process redemption count increment
            coupon.setTimesRedeemed(coupon.getTimesRedeemed() + 1);
            couponRepository.save(coupon);

            // Record per-user redemption
            if (userId != null && coupon.getMaxRedemptionsPerUser() != null) {
                CouponRedemption redemption = redemptionRepository.findByCouponIdAndUserId(coupon.getId(), userId)
                        .orElse(CouponRedemption.builder()
                                .couponId(coupon.getId())
                                .userId(userId)
                                .redemptionCount(0)
                                .build());
                redemption.setRedemptionCount(redemption.getRedemptionCount() + 1);
                redemptionRepository.save(redemption);
            }

            BigDecimal discountForThisCoupon = currentPremium.multiply(coupon.getDiscountPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            totalDiscount = totalDiscount.add(discountForThisCoupon);
            currentPremium = currentPremium.subtract(discountForThisCoupon);
        }

        return totalDiscount;
    }

    public java.util.List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public java.util.List<Coupon> getActiveCoupons() {
        return couponRepository.findByActiveTrue();
    }

    public com.odmip.pricing.dto.CouponValidationResponse validateCoupon(String code, Long userId) {
        java.util.Optional<Coupon> couponOpt = couponRepository.findByCodeAndActiveTrue(code.toUpperCase());
        if (couponOpt.isEmpty()) {
            return new com.odmip.pricing.dto.CouponValidationResponse(false, "Coupon not found or is inactive");
        }
        Coupon coupon = couponOpt.get();
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom())) {
            return new com.odmip.pricing.dto.CouponValidationResponse(false, "Coupon is not active yet");
        }
        if (now.isAfter(coupon.getValidUntil())) {
            return new com.odmip.pricing.dto.CouponValidationResponse(false, "Coupon has expired");
        }
        if (coupon.getTimesRedeemed() >= coupon.getMaxRedemptions()) {
            return new com.odmip.pricing.dto.CouponValidationResponse(false, "Coupon has reached its maximum redemption limit");
        }
        if (userId != null && coupon.getMaxRedemptionsPerUser() != null) {
            int userRedemptions = redemptionRepository.findByCouponIdAndUserId(coupon.getId(), userId)
                    .map(CouponRedemption::getRedemptionCount)
                    .orElse(0);
            if (userRedemptions >= coupon.getMaxRedemptionsPerUser()) {
                return new com.odmip.pricing.dto.CouponValidationResponse(false, "Coupon has reached its redemption limit for this user");
            }
        }
        return new com.odmip.pricing.dto.CouponValidationResponse(true, "Coupon is valid");
    }
}
