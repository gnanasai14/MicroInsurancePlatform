package com.odmip.user.dto;

/** Returned by POST /api/auth/register - no JWT yet, email must be OTP-verified first. */
public record RegisterResponse(
        String username,
        String email,
        String message
) {}
