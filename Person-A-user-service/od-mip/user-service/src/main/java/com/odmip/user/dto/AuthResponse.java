package com.odmip.user.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        Set<String> roles
) {
    public AuthResponse(String token, String username, Set<String> roles) {
        this(token, "Bearer", username, roles);
    }
}
