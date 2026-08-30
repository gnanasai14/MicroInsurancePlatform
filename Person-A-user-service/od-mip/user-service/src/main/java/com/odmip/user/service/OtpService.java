package com.odmip.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Generates and "sends" the 6-digit OTP used to verify a new account's email
 * before login is allowed. mail-enabled defaults to false so this works out
 * of the box with zero SMTP setup - the OTP is logged instead of emailed.
 * Flip odmip.otp.mail-enabled + supply real spring.mail.* creds to send for real.
 */
@Service
@Slf4j
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;

    public OtpService(JavaMailSender mailSender, @Value("${odmip.otp.mail-enabled:false}") boolean mailEnabled) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
    }

    public String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000); // always 6 digits
        return String.valueOf(code);
    }

    public void sendOtp(String toEmail, String username, String otp) {
        if (!mailEnabled) {
            log.info("=== DEV MODE (odmip.otp.mail-enabled=false) - no email sent ===");
            log.info("OTP for {} ({}): {}", username, toEmail, otp);
            log.info("===============================================================");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("OD-MIP - verify your account");
            message.setText("Hi " + username + ",\n\nYour verification code is: " + otp
                    + "\n\nThis code expires in 10 minutes.\n\n- OD-MIP");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
            log.info("Falling back to console - OTP for {} ({}): {}", username, toEmail, otp);
        }
    }
}
