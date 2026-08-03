package com.odmip.claims.service;

import com.odmip.claims.client.PolicyServiceClient;
import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import com.odmip.claims.entity.ClaimValidationRetry;
import com.odmip.claims.entity.FraudFlag;
import com.odmip.claims.repository.ClaimRepository;
import com.odmip.claims.repository.ClaimValidationRetryRepository;
import com.odmip.common.event.FraudFlaggedEvent;
import com.odmip.claims.notification.NotificationPublisher;
import com.odmip.common.exception.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimValidationRetryScheduler {

    private final ClaimValidationRetryRepository retryRepository;
    private final ClaimRepository claimRepository;
    private final PolicyServiceClient policyServiceClient;
    private final RiskScoringService riskScoringService;
    private final FraudDetectionService fraudDetectionService;
    private final ClaimService claimService;
    private final NotificationPublisher notificationPublisher;

    @org.springframework.beans.factory.annotation.Value("${odmip.claims.retry.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelay = 10000) // retry every 10 seconds
    @Transactional
    public void processPendingRetries() {
        List<ClaimValidationRetry> retries = retryRepository.findByStatusAndNextRetryAtBefore("PENDING", LocalDateTime.now());
        if (retries.isEmpty()) {
            return;
        }

        log.info("Found {} pending policy validation retries to process.", retries.size());

        for (ClaimValidationRetry retry : retries) {
            Claim claim = claimRepository.findById(retry.getClaimId()).orElse(null);
            if (claim == null) {
                log.warn("Claim {} for retry not found. Marking retry as FAILED.", retry.getClaimId());
                retry.setStatus("FAILED");
                retry.setLastError("Claim not found");
                retryRepository.save(retry);
                continue;
            }

            try {
                boolean active = policyServiceClient.isPolicyActive(retry.getPolicyId(), retry.getUserId());
                if (active) {
                    log.info("Policy {} is active for user {}. Resolving retry for claim {}.", 
                            retry.getPolicyId(), retry.getUserId(), claim.getId());
                    
                    claim.setPolicyValidated(true);
                    claimRepository.save(claim);

                    int recentClaims = claimRepository
                            .findByUserIdAndSubmittedAtAfter(retry.getUserId(), LocalDateTime.now().minusDays(30))
                            .size();

                    List<FraudFlag> flags = fraudDetectionService.evaluate(claim, recentClaims);
                    riskScoringService.score(claim, recentClaims, flags);

                    if (!flags.isEmpty()) {
                        claimService.updateStatus(claim.getId(), ClaimStatus.ON_HOLD, 
                                "Auto-held: " + flags.size() + " fraud rule(s) triggered");
                        flags.forEach(f -> notificationPublisher.publishFraudFlagged(
                                new FraudFlaggedEvent(claim.getId(), claim.getPolicyId(), f.getReason(), 0, Instant.now())));
                    } else {
                        claimService.updateStatus(claim.getId(), ClaimStatus.VALIDATED, 
                                "Passed automated validation + fraud checks");
                    }

                    retry.setStatus("SUCCESS");
                    retryRepository.save(retry);
                } else {
                    log.info("Policy {} is not ACTIVE or does not exist. Rejecting claim {}.", 
                            retry.getPolicyId(), claim.getId());
                    claim.setPolicyValidated(false);
                    claimRepository.save(claim);

                    claimService.updateStatus(claim.getId(), ClaimStatus.REJECTED, 
                            "Policy validation failed: policy does not exist or is not ACTIVE.");

                    retry.setStatus("FAILED");
                    retry.setLastError("Policy not active or not found");
                    retryRepository.save(retry);
                }
            } catch (UserServiceUnavailableException ex) {
                log.warn("User service still unavailable for retry of claim {}: {}", claim.getId(), ex.getMessage());
                int count = retry.getRetryCount() + 1;
                retry.setRetryCount(count);
                retry.setLastError(ex.getMessage());
                if (count >= maxAttempts) {
                    log.error("Max retries exceeded for claim {}. Rejecting.", claim.getId());
                    claim.setPolicyValidated(false);
                    claimRepository.save(claim);
                    claimService.updateStatus(claim.getId(), ClaimStatus.REJECTED, 
                            "Policy validation failed: User service unavailable after max retries.");
                    retry.setStatus("DEAD_LETTER");
                } else {
                    retry.setNextRetryAt(LocalDateTime.now().plusSeconds(15L * count));
                }
                retryRepository.save(retry);
            } catch (Exception ex) {
                log.error("Unexpected error processing retry for claim {}: {}", claim.getId(), ex.getMessage(), ex);
                retry.setLastError(ex.getMessage());
                retry.setStatus("FAILED");
                retryRepository.save(retry);
            }
        }
    }
}
