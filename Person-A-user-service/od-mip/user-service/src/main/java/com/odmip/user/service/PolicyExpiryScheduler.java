package com.odmip.user.service;

import com.odmip.common.event.PolicyExpiringEvent;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import com.odmip.user.notification.PolicyEventPublisher;
import com.odmip.user.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Policy Activation & Expiry Engine.
 * Runs every 5 minutes, flips ACTIVE policies whose endDate has passed to EXPIRED.
 */
@Component
public class PolicyExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryScheduler.class);

    private final PolicyRepository policyRepository;
    private final PolicyEventPublisher policyEventPublisher;

    public PolicyExpiryScheduler(PolicyRepository policyRepository, PolicyEventPublisher policyEventPublisher) {
        this.policyRepository = policyRepository;
        this.policyEventPublisher = policyEventPublisher;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expireOverduePolicies() {
        List<Policy> overdue = policyRepository.findByStatusAndEndDateBefore(PolicyStatus.ACTIVE, LocalDateTime.now());
        if (overdue.isEmpty()) return;

        for (Policy policy : overdue) {
            policy.setStatus(PolicyStatus.EXPIRED);
            log.info("Policy {} expired (endDate={})", policy.getPolicyNumber(), policy.getEndDate());
            
            // Publish PolicyExpiringEvent to SNS topic
            policyEventPublisher.publishPolicyExpiring(new PolicyExpiringEvent(
                    policy.getId(),
                    policy.getPolicyNumber(),
                    policy.getUserId(),
                    policy.getEndDate(),
                    Instant.now()
            ));
        }
        policyRepository.saveAll(overdue);
    }
}
