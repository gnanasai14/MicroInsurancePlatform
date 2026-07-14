package com.odmip.common.dto;

import java.util.Set;

/** Minimal, non-sensitive user projection shared across services. */
public record UserDTO(
        Long id,
        String username,
        String email,
        Set<String> roles
) {}
