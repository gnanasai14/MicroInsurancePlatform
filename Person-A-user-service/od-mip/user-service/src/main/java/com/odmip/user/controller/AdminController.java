package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import com.odmip.user.entity.Role;
import com.odmip.user.entity.User;
import com.odmip.user.repository.UserRepository;
import com.odmip.user.repository.UserSpecification;
import com.odmip.user.repository.PolicySpecification;
import com.odmip.user.service.PolicyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public ApiResponse<Page<User>> allUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<User> spec = Specification.where(UserSpecification.hasUsername(username))
                .and(UserSpecification.hasEmail(email))
                .and(UserSpecification.hasRole(role))
                .and(UserSpecification.hasEnabled(enabled));
        return ApiResponse.ok(userRepository.findAll(spec, pageable));
    }

    @GetMapping("/policies")
    public ApiResponse<Page<Policy>> allPolicies(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) String templateCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Policy> spec = Specification.where(PolicySpecification.hasUserId(userId))
                .and(PolicySpecification.hasStatus(status))
                .and(PolicySpecification.hasPolicyNumber(policyNumber))
                .and(PolicySpecification.hasTemplateCode(templateCode));
        return ApiResponse.ok(policyService.findAll(spec, pageable));
    }

    @PostMapping("/users/{id}/disable")
    public ApiResponse<User> disableUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("No user " + id));
        user.setEnabled(false);
        return ApiResponse.ok("User disabled", userRepository.save(user));
    }
}
