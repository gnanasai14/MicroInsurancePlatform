package com.odmip.common.event;

import java.time.Instant;

public record ClaimStatusChangedEvent(
    Long claimId,
    Long policyId,
    Long userId,
    String oldStatus,
    String newStatus,
    Instant timestamp
) {}
