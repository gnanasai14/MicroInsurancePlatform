package com.odmip.common.event;

import java.time.Instant;

/**
 * SNS event contract - published by risk-fraud module when a claim is
 * flagged. Consumed by notification-service (to alert an underwriter/admin)
 * and optionally by claims-service (to auto-hold the claim).
 * Topic (proposed): odmip-fraud-events
 */
public record FraudFlaggedEvent(
        Long claimId,
        Long policyId,
        String reason,
        int riskScore,
        Instant emittedAt
) {}
