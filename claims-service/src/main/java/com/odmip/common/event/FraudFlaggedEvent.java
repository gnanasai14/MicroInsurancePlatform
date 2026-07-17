package com.odmip.common.event;

import java.time.Instant;

public record FraudFlaggedEvent(
    Long claimId,
    Long policyId,
    String reason,
    int riskScore,
    Instant timestamp
) {}
