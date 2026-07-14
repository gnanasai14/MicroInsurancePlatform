package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.User;
import com.odmip.user.repository.UserRepository;
import com.odmip.user.service.PolicyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Panel (Week 2 scope: read/manage endpoints).
 * Locked to ROLE_ADMIN in SecurityConfig ("/api/admin/**").
 * Week 3+: wire in pricing-rule management + user suspension once
 * pricing-service's PricingRule entity is stable.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin-only: manage users & policies")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final PolicyService policyService;

    public AdminController(UserRepository userRepository, PolicyService policyService) {
        this.userRepository = userRepository;
        this.policyService = policyService;
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> allUsers() {
        return ApiResponse.ok(userRepository.findAll());
    }

    @GetMapping("/policies")
    public ApiResponse<List<Policy>> allPolicies() {
        return ApiResponse.ok(policyService.findAll());
    }

    @PostMapping("/users/{id}/disable")
    public ApiResponse<User> disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("No user " + id));
        user.setEnabled(false);
        return ApiResponse.ok("User disabled", userRepository.save(user));
    }
}
