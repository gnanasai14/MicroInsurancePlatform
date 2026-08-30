package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.user.dto.AuthResponse;
import com.odmip.user.dto.LoginRequest;
import com.odmip.user.dto.RegisterRequest;
import com.odmip.user.dto.RegisterResponse;
import com.odmip.user.dto.ResendOtpRequest;
import com.odmip.user.dto.VerifyOtpRequest;
import com.odmip.user.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration & login (JWT), email OTP verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Creates the account unverified and emails (or logs, in dev mode) a 6-digit OTP. No token yet. */
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Verification code sent", authService.register(request));
    }

    /** Verifies the OTP and, on success, logs the user in immediately. */
    @PostMapping("/verify-otp")
    public ApiResponse<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ApiResponse.ok("Email verified", authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ApiResponse<RegisterResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ApiResponse.ok("New code sent", authService.resendOtp(request.username()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authService.login(request));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<com.odmip.common.dto.UserDTO> getUser(@PathVariable Long id) {
        return ApiResponse.ok(authService.getUserById(id));
    }

    /** Resolves the calling JWT's own user record - the frontend needs this to learn its own numeric userId. */
    @GetMapping("/me")
    public ApiResponse<com.odmip.common.dto.UserDTO> me(java.security.Principal principal) {
        return ApiResponse.ok(authService.getCurrentUser(principal.getName()));
    }
}
