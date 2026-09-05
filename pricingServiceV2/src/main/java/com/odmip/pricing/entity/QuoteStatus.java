package com.odmip.pricing.entity;

/**
 * PENDING: quote calculated and shown to the customer, nothing committed yet.
 * ACCEPTED: customer clicked "Pay" - premium pushed to the policy, policy
 *           activated if it was still DRAFT, confirmation email sent.
 * CANCELLED: customer backed out at the payment step. No side effects.
 */
public enum QuoteStatus {
    PENDING,
    ACCEPTED,
    CANCELLED
}
