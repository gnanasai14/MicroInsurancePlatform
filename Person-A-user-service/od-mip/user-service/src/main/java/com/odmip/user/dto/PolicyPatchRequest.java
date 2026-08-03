package com.odmip.user.dto;

import com.odmip.user.entity.PolicyStatus;

import java.math.BigDecimal;

public record PolicyPatchRequest(
        BigDecimal premium,
        PolicyStatus status
) {}
