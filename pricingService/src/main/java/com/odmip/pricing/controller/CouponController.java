package com.odmip.pricing.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.pricing.dto.CouponRequest;
import com.odmip.pricing.entity.Coupon;
import com.odmip.pricing.service.CouponService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "Coupons", description = "Discount & coupon management")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ApiResponse<Coupon> create(@Valid @RequestBody CouponRequest request) {
        return ApiResponse.ok("Coupon created", couponService.create(request));
    }
}
