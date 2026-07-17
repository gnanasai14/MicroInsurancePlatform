package com.odmip.claims.service;

import com.odmip.claims.client.PolicyServiceClient;
import com.odmip.claims.dto.ClaimSubmitRequest;
import com.odmip.common.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ClaimValidationService {

    private static final BigDecimal MAX_REASONABLE_CLAIM = new BigDecimal("1000000");
    private final PolicyServiceClient policyServiceClient;

    public void validateBasic(ClaimSubmitRequest req) {
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

    public void validate(ClaimSubmitRequest req) {
        validateBasic(req);

        // Live policy validation against user-service (Port 8081)
        boolean active = policyServiceClient.isPolicyActive(req.policyId(), req.userId());
        if (!active) {
            throw new BusinessRuleException("Policy " + req.policyId() + " is not ACTIVE for user " + req.userId());
        }
    }
}
