package com.odmip.pricing.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.pricing.dto.CouponRequest;
import com.odmip.pricing.entity.Coupon;
import com.odmip.pricing.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
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
                .timesRedeemed(0)
                .active(true)
                .build();
        return couponRepository.save(coupon);
    }

    /** Validates + "redeems" a coupon and returns the discount amount off the given premium. */
    public BigDecimal calculateDiscount(String code, BigDecimal premium) {
        Coupon coupon = couponRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("No active coupon: " + code));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new BusinessRuleException("Coupon " + code + " is not valid right now");
        }
        if (coupon.getTimesRedeemed() >= coupon.getMaxRedemptions()) {
            throw new BusinessRuleException("Coupon " + code + " has reached its redemption limit");
        }

        coupon.setTimesRedeemed(coupon.getTimesRedeemed() + 1);
        couponRepository.save(coupon);

        return premium.multiply(coupon.getDiscountPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
