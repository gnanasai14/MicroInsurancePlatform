package com.odmip.common.exception;

/**
 * Local stand-in for the shared `common` module's BusinessRuleException,
 * recreated here because the real `common` module wasn't included in this
 * handoff. Thrown for domain rule violations (e.g. invalid coupon state).
 * Replace with the real shared dependency once available.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
