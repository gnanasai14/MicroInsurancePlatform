package com.odmip.common.event;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * SNS event contract - published by policy-service (Person A) when a policy
 * is nearing/hits expiry, consumed by notification-service (Person C).
 * Topic (proposed): odmip-policy-events
 */
public record PolicyExpiringEvent(
        Long policyId,
        String policyNumber,
        Long userId,
        LocalDateTime expiryDate,
        Instant emittedAt
) {}
