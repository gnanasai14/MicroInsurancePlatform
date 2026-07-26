package com.odmip.user.dto;

public record UpdatePreferencesRequest(
        Boolean emailAlertsEnabled,
        Boolean smsAlertsEnabled
) {}
