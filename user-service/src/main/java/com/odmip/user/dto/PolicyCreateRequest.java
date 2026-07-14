package com.odmip.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PolicyCreateRequest(
        @NotNull Long userId,
        @NotBlank String templateCode,
        Integer durationHoursOverride // optional - defaults to template's defaultDurationHours
) {}
