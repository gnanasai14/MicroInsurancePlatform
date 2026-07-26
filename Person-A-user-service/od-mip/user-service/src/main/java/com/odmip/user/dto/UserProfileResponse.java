package com.odmip.user.dto;

import java.util.Set;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String status,
        Set<String> roles,
        boolean emailAlertsEnabled,
        boolean smsAlertsEnabled
) {}
