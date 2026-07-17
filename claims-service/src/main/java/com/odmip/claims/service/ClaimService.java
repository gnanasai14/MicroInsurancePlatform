package com.odmip.claims.service;

import com.odmip.claims.client.PolicyServiceClient;
import com.odmip.claims.dto.ClaimSubmitRequest;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import com.odmip.claims.entity.ClaimValidationRetry;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.notification.NotificationPublisher;
import com.odmip.claims.repository.ClaimRepository;
import com.odmip.claims.repository.ClaimValidationRetryRepository;
import com.odmip.common.event.ClaimStatusChangedEvent;
import com.odmip.common.event.FraudFlaggedEvent;
import com.odmip.common.exception.BusinessRuleException;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.common.exception.UserServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Orchestrates the claim lifecycle:
 *   submit -> validate -> persist (SUBMITTED) -> risk-score -> fraud-check
 *          -> auto VALIDATED, or ON_HOLD if fraud flags were raised
 * Status Tracking is a simple explicit state machine (ALLOWED_TRANSITIONS
 * below) enforced on every update.
 */
@Service
@Slf4j
public class ClaimService {

    private static final Map<ClaimStatus, Set<ClaimStatus>> ALLOWED_TRANSITIONS = Map.of(
            ClaimStatus.SUBMITTED, Set.of(ClaimStatus.VALIDATED, ClaimStatus.ON_HOLD, ClaimStatus.REJECTED),
            ClaimStatus.VALIDATED, Set.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.ON_HOLD, ClaimStatus.REJECTED),
            ClaimStatus.UNDER_REVIEW, Set.of(ClaimStatus.APPROVED, ClaimStatus.REJECTED, ClaimStatus.ON_HOLD),
            ClaimStatus.ON_HOLD, Set.of(ClaimStatus.UNDER_REVIEW, ClaimStatus.REJECTED),
            ClaimStatus.APPROVED, Set.of(),
            ClaimStatus.REJECTED, Set.of()
    );

    private final ClaimRepository claimRepository;
    private final ClaimValidationService validationService;
    private final RiskScoringService riskScoringService;
    private final FraudDetectionService fraudDetectionService;
    private final NotificationPublisher notificationPublisher;
    private final PolicyServiceClient policyServiceClient;
    private final ClaimValidationRetryRepository retryRepository;

    public ClaimService(ClaimRepository claimRepository, ClaimValidationService validationService,
                         RiskScoringService riskScoringService, FraudDetectionService fraudDetectionService,
                         NotificationPublisher notificationPublisher, PolicyServiceClient policyServiceClient,
                         ClaimValidationRetryRepository retryRepository) {
        this.claimRepository = claimRepository;
        this.validationService = validationService;
        this.riskScoringService = riskScoringService;
        this.fraudDetectionService = fraudDetectionService;
        this.notificationPublisher = notificationPublisher;
        this.policyServiceClient = policyServiceClient;
        this.retryRepository = retryRepository;
    }

    public Claim submit(ClaimSubmitRequest req) {
        validationService.validateBasic(req);

        Claim claim = Claim.builder()
                .claimNumber(generateClaimNumber())
                .policyId(req.policyId())
                .userId(req.userId())
                .claimedAmount(req.claimedAmount())
                .description(req.description())
                .status(ClaimStatus.SUBMITTED)
                .build();

        boolean serviceDown = false;
        boolean active = false;

        try {
            active = policyServiceClient.isPolicyActive(req.policyId(), req.userId());
        } catch (UserServiceUnavailableException ex) {
            log.warn("User service unavailable during claim submission. Storing claim as validation pending. Error: {}", ex.getMessage());
            serviceDown = true;
        }

        if (serviceDown) {
            claim.setPolicyValidated(false);
            Claim savedClaim = claimRepository.save(claim);

            // Queue validation retry task
            ClaimValidationRetry retry = ClaimValidationRetry.builder()
                    .claimId(savedClaim.getId())
                    .policyId(req.policyId())
                    .userId(req.userId())
                    .retryCount(0)
                    .status("PENDING")
                    .nextRetryAt(LocalDateTime.now())
                    .lastError("User service unavailable on initial submission")
                    .build();
            retryRepository.save(retry);

            return savedClaim;
        }

        if (!active) {
            throw new BusinessRuleException("Policy " + req.policyId() + " is not ACTIVE for user " + req.userId());
        }

        claim.setPolicyValidated(true);
        final Claim savedClaim = claimRepository.save(claim);

        int recentClaims = claimRepository
                .findByUserIdAndSubmittedAtAfter(req.userId(), LocalDateTime.now().minusDays(30))
                .size();

        // Swap order: run fraud detection first so risk scoring can factor in current rule weights
        List<FraudFlag> flags = fraudDetectionService.evaluate(savedClaim, recentClaims);
        riskScoringService.score(savedClaim, recentClaims, flags);

        if (!flags.isEmpty()) {
            transition(savedClaim, ClaimStatus.ON_HOLD, "Auto-held: " + flags.size() + " fraud rule(s) triggered");
            flags.forEach(f -> notificationPublisher.publishFraudFlagged(
                    new FraudFlaggedEvent(savedClaim.getId(), savedClaim.getPolicyId(), f.getReason(), 0, Instant.now())));
        } else {
            transition(savedClaim, ClaimStatus.VALIDATED, "Passed automated validation + fraud checks");
        }

        return savedClaim;
    }

    public Claim updateStatus(Long claimId, ClaimStatus newStatus, String note) {
        Claim claim = getById(claimId);
        transition(claim, newStatus, note);
        return claim;
    }

    public Claim getById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No claim with id " + id));
    }

    public List<Claim> findByPolicy(Long policyId) {
        return claimRepository.findByPolicyId(policyId);
    }

    public List<Claim> findByUser(Long userId) {
        return claimRepository.findByUserId(userId);
    }

    private void transition(Claim claim, ClaimStatus newStatus, String note) {
        ClaimStatus current = claim.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(newStatus)) {
            throw new BusinessRuleException(
                    "Illegal transition " + current + " -> " + newStatus + " for claim " + claim.getClaimNumber());
        }

        claim.setStatus(newStatus);
        claimRepository.save(claim);

        notificationPublisher.publishClaimStatusChanged(new ClaimStatusChangedEvent(
                claim.getId(), claim.getPolicyId(), claim.getUserId(),
                current.name(), newStatus.name(), Instant.now()));
    }

    private String generateClaimNumber() {
        return "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
