package com.odmip.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email(message = "Invalid email contact details") @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email contact details") String email,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid SMS contact details") String sms,
        @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String password
) {}
