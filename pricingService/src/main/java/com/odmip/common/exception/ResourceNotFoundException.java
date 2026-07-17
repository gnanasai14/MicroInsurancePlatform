package com.odmip.common.exception;

/**
 * Local stand-in for the shared `common` module's ResourceNotFoundException,
 * recreated here because the real `common` module wasn't included in this
 * handoff. Thrown when a lookup (e.g. coupon code) finds nothing.
 * Replace with the real shared dependency once available.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
