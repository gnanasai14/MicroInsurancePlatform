package com.odmip.user.dto;

public record UserPreferencesResponse(
        boolean emailAlertsEnabled,
        boolean smsAlertsEnabled
) {}
