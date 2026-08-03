package com.odmip.pricing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public void sendQuoteConfirmation(String toEmail, String policyNumber, BigDecimal premium) {
        log.info("[EMAIL CONFIRMATION] Sending email to {}. Confirmation for Policy: {}. Final Premium: ${}", toEmail, policyNumber, premium);
    }

    public void sendCapWarning(String toEmail, String policyNumber, double percentage) {
        log.info("[EMAIL ALERT] Sending warning email to {}. Policy: {} has reached {}% of its usage cap!", toEmail, policyNumber, percentage);
    }
}
