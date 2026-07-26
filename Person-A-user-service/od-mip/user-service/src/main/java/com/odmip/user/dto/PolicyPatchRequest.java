package com.odmip.user.dto;

import java.math.BigDecimal;

public record PolicyPatchRequest(
        String status,
        BigDecimal premiumAmount
) {}
