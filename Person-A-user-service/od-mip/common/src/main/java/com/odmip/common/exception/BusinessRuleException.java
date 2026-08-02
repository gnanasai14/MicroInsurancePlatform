package com.odmip.common.exception;

/** Thrown for domain-rule violations (e.g. invalid policy transition, duplicate coupon). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
