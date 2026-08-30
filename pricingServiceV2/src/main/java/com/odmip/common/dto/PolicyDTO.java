package com.odmip.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Local stand-in for the shared `common` module's PolicyDTO, recreated here
 * because the real `common` module wasn't included in this handoff.
 * Fields included are the ones actually referenced in pricing-service
 * (id, policyNumber, status) plus userId for context.
 * ignoreUnknown = true so this still deserializes fine even if the real
 * user-service response includes extra fields we don't use here.
 * Replace with the real shared dependency once available.
 *
 * usageCap: previously always null - user-service's Policy JSON had no such
 * field. user-service now exposes a top-level "usageCap" (sourced from
 * PolicyTemplate.usageCap, see Policy.getUsageCap()), so this now populates
 * correctly for templates that set one. Templates that leave it unset still
 * come back null; UsageTrackingService's 100.0 fallback still applies there.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyDTO(Long id, String policyNumber, String status, Long userId, Double usageCap) {}
