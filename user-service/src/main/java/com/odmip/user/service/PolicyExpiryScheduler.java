package com.odmip.user.service;

import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import com.odmip.user.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Policy Activation & Expiry Engine.
 * Runs every 5 minutes, flips ACTIVE policies whose endDate has passed to EXPIRED.
 *
 * Week 3+ TODO (flagged for Person C): instead of just flipping status, publish
 * a PolicyExpiringEvent to SNS topic `odmip-policy-events` so notification-service
 * can alert the user. For now this just logs - wire in the SNS publisher once
 * AWS credentials/topic ARNs are available to the team.
 */
@Component
public class PolicyExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryScheduler.class);

    private final PolicyRepository policyRepository;

    public PolicyExpiryScheduler(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expireOverduePolicies() {
        List<Policy> overdue = policyRepository.findByStatusAndEndDateBefore(PolicyStatus.ACTIVE, LocalDateTime.now());
        if (overdue.isEmpty()) return;

        for (Policy policy : overdue) {
            policy.setStatus(PolicyStatus.EXPIRED);
            log.info("Policy {} expired (endDate={})", policy.getPolicyNumber(), policy.getEndDate());
            // TODO: publish PolicyExpiringEvent here once SNS wiring lands
        }
        policyRepository.saveAll(overdue);
    }
}
