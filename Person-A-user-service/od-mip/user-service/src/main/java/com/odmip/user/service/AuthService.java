package com.odmip.user.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.user.dto.AuthResponse;
import com.odmip.user.dto.LoginRequest;
import com.odmip.user.dto.RegisterRequest;
import com.odmip.user.dto.RegisterResponse;
import com.odmip.user.dto.VerifyOtpRequest;
import com.odmip.user.entity.Role;
import com.odmip.user.entity.User;
import com.odmip.user.repository.UserRepository;
import com.odmip.user.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final long otpExpiryMinutes;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                        OtpService otpService,
                        @org.springframework.beans.factory.annotation.Value("${odmip.otp.expiry-minutes:10}") long otpExpiryMinutes) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.otpExpiryMinutes = otpExpiryMinutes;
    }

    /** Creates the account unverified and emails (or, in dev mode, logs) a 6-digit OTP. No login yet. */
    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessRuleException("Username already taken: " + req.username());
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessRuleException("Email already registered: " + req.email());
        }

        String otp = otpService.generateOtp();

        User user = User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .username(req.username())
                .email(req.email())
                .sms(req.sms())
                .password(passwordEncoder.encode(req.password()))
                .enabled(true)
                .emailVerified(false)
                .otpCode(otp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userRepository.save(user);
        otpService.sendOtp(user.getEmail(), user.getUsername(), otp);

        return new RegisterResponse(user.getUsername(), user.getEmail(),
                "Verification code sent to " + maskEmail(user.getEmail()));
    }

    /** Verifies the OTP and, on success, logs the user straight in. */
    public AuthResponse verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessRuleException("No such user: " + req.username()));

        if (user.isEmailVerified()) {
            throw new BusinessRuleException("This account is already verified - please sign in.");
        }
        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null) {
            throw new BusinessRuleException("No verification code is pending - request a new one.");
        }
        if (LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
            throw new BusinessRuleException("That code has expired - request a new one.");
        }
        if (!user.getOtpCode().equals(req.otp())) {
            throw new BusinessRuleException("Incorrect verification code.");
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), roleNames(user));
        return new AuthResponse(token, user.getUsername(), toStringSet(user.getRoles()));
    }

    /** Issues a fresh OTP for a not-yet-verified account (e.g. the first one expired). */
    public RegisterResponse resendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessRuleException("No such user: " + username));
        if (user.isEmailVerified()) {
            throw new BusinessRuleException("This account is already verified - please sign in.");
        }

        String otp = otpService.generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        userRepository.save(user);
        otpService.sendOtp(user.getEmail(), user.getUsername(), otp);

        return new RegisterResponse(user.getUsername(), user.getEmail(),
                "New verification code sent to " + maskEmail(user.getEmail()));
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (DisabledException ex) {
            User pending = userRepository.findByUsername(req.username()).orElse(null);
            if (pending != null && !pending.isEmailVerified()) {
                throw new BusinessRuleException("Please verify your email before signing in.");
            }
            throw new BusinessRuleException("This account has been disabled.");
        } catch (BadCredentialsException ex) {
            throw new BusinessRuleException("Invalid username or password.");
        }

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessRuleException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getUsername(), roleNames(user));
        return new AuthResponse(token, user.getUsername(), toStringSet(user.getRoles()));
    }

    public com.odmip.common.dto.UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("No user with id " + id));
        return new com.odmip.common.dto.UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    /** Used by GET /api/auth/me - resolves the authenticated principal's own record. */
    public com.odmip.common.dto.UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.odmip.common.exception.ResourceNotFoundException("No user with username " + username));
        return new com.odmip.common.dto.UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }

    private java.util.List<String> roleNames(User user) {
        return user.getRoles().stream().map(Enum::name).toList();
    }

    private Set<String> toStringSet(Set<Role> roles) {
        return roles.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
