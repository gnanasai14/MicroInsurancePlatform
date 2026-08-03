package com.odmip.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDTO(
        Long id,
        String username,
        String email,
        Set<String> roles
) {}
