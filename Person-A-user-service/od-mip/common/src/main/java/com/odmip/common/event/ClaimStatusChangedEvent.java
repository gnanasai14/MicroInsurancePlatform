package com.odmip.common.event;

import java.time.Instant;

/**
 * SNS event contract - published internally by claims-service when a
 * claim's status transitions (SUBMITTED -> VALIDATED -> APPROVED / REJECTED).
 * Topic (proposed): odmip-claim-events
 */
public record ClaimStatusChangedEvent(
        Long claimId,
        Long policyId,
        Long userId,
        String previousStatus,
        String newStatus,
        Instant emittedAt
) {}
