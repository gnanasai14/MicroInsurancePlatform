package com.odmip.claims.entity;

/**
 * State machine for a claim:
 *   SUBMITTED -> VALIDATED -> UNDER_REVIEW -> APPROVED
 *                                          -> REJECTED
 *   (any state) -> ON_HOLD  (fraud flag)
 */
public enum ClaimStatus {
    SUBMITTED,
    VALIDATED,
    UNDER_REVIEW,
    ON_HOLD,
    APPROVED,
    REJECTED
}
