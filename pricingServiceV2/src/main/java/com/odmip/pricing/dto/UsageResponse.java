package com.odmip.pricing.dto;

import com.odmip.pricing.entity.UsageLog;

/**
 * Wraps the saved UsageLog with threshold info, so the frontend can show a
 * live "you just crossed 80%" banner instead of that only being visible in
 * pricing-service's server log (which is all that existed before).
 * thresholdCrossed is null on a normal log; "WARNING_80_PERCENT" or
 * "CAP_REACHED" the moment that specific threshold is newly crossed.
 */
public record UsageResponse(
        UsageLog usageLog,
        double totalUsage,
        Double usageCap,
        double percentage,
        String thresholdCrossed
) {}
