package com.odmip.user.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.user.dto.AuthResponse;
import com.odmip.user.dto.LoginRequest;
import com.odmip.user.dto.RegisterRequest;
import com.odmip.user.entity.Role;
import com.odmip.user.entity.User;
import com.odmip.user.repository.UserRepository;
import com.odmip.user.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessRuleException("Username already taken: " + req.username());
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessRuleException("Email already registered: " + req.email());
        }

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .enabled(true)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), roleNames(user));
        return new AuthResponse(token, user.getUsername(), toStringSet(user.getRoles()));
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessRuleException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getUsername(), roleNames(user));
        return new AuthResponse(token, user.getUsername(), toStringSet(user.getRoles()));
    }

    private java.util.List<String> roleNames(User user) {
        return user.getRoles().stream().map(Enum::name).toList();
    }

    private Set<String> toStringSet(Set<Role> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
