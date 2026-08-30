package com.odmip.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 50) String firstName,
        @NotBlank @Size(min = 2, max = 50) String lastName,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email(message = "Invalid email address")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email must be a @gmail.com address")
        String email,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid SMS contact details") String sms,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$",
                message = "Password must include an uppercase letter, a lowercase letter, a number, and a special character"
        )
        String password
) {}
