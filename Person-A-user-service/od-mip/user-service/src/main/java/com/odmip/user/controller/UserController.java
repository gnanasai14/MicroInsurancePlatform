package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.user.dto.RegisterRequest;
import com.odmip.user.dto.RegisterResponse;
import com.odmip.user.dto.UpdatePreferencesRequest;
import com.odmip.user.dto.UserProfileResponse;
import com.odmip.user.dto.UserPreferencesResponse;
import com.odmip.user.entity.User;
import com.odmip.user.repository.UserRepository;
import com.odmip.user.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User registration, profile management, and notification preferences")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Verification code sent", authService.register(request));
    }

    @GetMapping("/{id}/profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserProfileResponse> getProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus().name(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                user.isEmailAlertsEnabled(),
                user.isSmsAlertsEnabled()
        );
        return ApiResponse.ok(response);
    }

    @GetMapping("/{id}/preferences")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserPreferencesResponse> getPreferences(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return ApiResponse.ok(new UserPreferencesResponse(
                user.isEmailAlertsEnabled(),
                user.isSmsAlertsEnabled()
        ));
    }

    @PatchMapping("/{id}/preferences")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserPreferencesResponse> patchPreferences(
            @PathVariable Long id,
            @RequestBody UpdatePreferencesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.emailAlertsEnabled() != null) {
            user.setEmailAlertsEnabled(request.emailAlertsEnabled());
        }
        if (request.smsAlertsEnabled() != null) {
            user.setSmsAlertsEnabled(request.smsAlertsEnabled());
        }

        userRepository.save(user);

        return ApiResponse.ok("Notification preferences updated", new UserPreferencesResponse(
                user.isEmailAlertsEnabled(),
                user.isSmsAlertsEnabled()
        ));
    }

    @PutMapping("/{id}/preferences")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<UserPreferencesResponse> putPreferences(
            @PathVariable Long id,
            @RequestBody UpdatePreferencesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.emailAlertsEnabled() == null || request.smsAlertsEnabled() == null) {
            throw new IllegalArgumentException("Both emailAlertsEnabled and smsAlertsEnabled must be provided for PUT update");
        }

        user.setEmailAlertsEnabled(request.emailAlertsEnabled());
        user.setSmsAlertsEnabled(request.smsAlertsEnabled());

        userRepository.save(user);

        return ApiResponse.ok("Notification preferences updated", new UserPreferencesResponse(
                user.isEmailAlertsEnabled(),
                user.isSmsAlertsEnabled()
        ));
    }
}
