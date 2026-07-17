package com.odmip.claims.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.claims.dto.ClaimSubmitRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ClaimValidationService {

    private static final BigDecimal MAX_REASONABLE_CLAIM = new BigDecimal("1000000");

    public void validate(ClaimSubmitRequest req) {
        if (req.claimedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Claimed amount must be positive");
        }
        if (req.claimedAmount().compareTo(MAX_REASONABLE_CLAIM) > 0) {
            throw new BusinessRuleException("Claimed amount exceeds maximum allowed (" + MAX_REASONABLE_CLAIM + ")");
        }
        if (req.description() != null && req.description().isBlank()) {
            throw new BusinessRuleException("Description cannot be blank if provided");
        }
    }
}
