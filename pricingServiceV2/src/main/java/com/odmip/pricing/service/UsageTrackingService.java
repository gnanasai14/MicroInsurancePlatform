package com.odmip.pricing.service;

import com.odmip.common.dto.PolicyDTO;
import com.odmip.pricing.client.PolicyServiceClient;
import com.odmip.pricing.dto.UsageRequest;
import com.odmip.pricing.entity.PolicyAlertState;
import com.odmip.pricing.entity.UsageLog;
import com.odmip.pricing.repository.PolicyAlertStateRepository;
import com.odmip.pricing.repository.UsageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsageTrackingService {

    private static final Logger log = LoggerFactory.getLogger(UsageTrackingService.class);

    private final UsageLogRepository usageLogRepository;
    private final PolicyServiceClient policyServiceClient;
    private final EmailNotificationService emailNotificationService;
    private final PolicyAlertStateRepository alertStateRepository;

    public UsageTrackingService(UsageLogRepository usageLogRepository, PolicyServiceClient policyServiceClient,
                                EmailNotificationService emailNotificationService,
                                PolicyAlertStateRepository alertStateRepository) {
        this.usageLogRepository = usageLogRepository;
        this.policyServiceClient = policyServiceClient;
        this.emailNotificationService = emailNotificationService;
        this.alertStateRepository = alertStateRepository;
    }

    public UsageLog record(UsageRequest req) {
        UsageLog usage = UsageLog.builder()
                .policyId(req.policyId())
                .userId(req.userId())
                .usageType(req.usageType())
                .quantity(req.quantity())
                .build();
        UsageLog saved = usageLogRepository.save(usage);

        // Threshold detection (80% / 100% of usage cap) - each threshold alerts once per policy.
        try {
            double total = totalUsage(req.policyId());
            PolicyDTO policy = policyServiceClient.getPolicy(req.policyId()).block();
            double cap = (policy != null && policy.usageCap() != null) ? policy.usageCap() : 100.0; // default cap is 100.0

            double percentage = (total / cap) * 100.0;

            PolicyAlertState alertState = alertStateRepository.findById(req.policyId())
                    .orElse(PolicyAlertState.builder().policyId(req.policyId()).build());

            if (percentage >= 100.0 && !alertState.isCapReachedSent()) {
                sendThresholdAlert(req, policy, total, cap, percentage, "CAP_REACHED", 100.0);
                alertState.setCapReachedSent(true);
                alertState.setWarning80Sent(true); // reaching 100% implies 80% is also covered
                alertStateRepository.save(alertState);
            } else if (percentage >= 80.0 && !alertState.isWarning80Sent()) {
                sendThresholdAlert(req, policy, total, cap, percentage, "WARNING_80_PERCENT", 80.0);
                alertState.setWarning80Sent(true);
                alertStateRepository.save(alertState);
            }
        } catch (Exception ex) {
            log.warn("Failed to check usage threshold alert for policy {}: {}", req.policyId(), ex.getMessage());
        }

        return saved;
    }

    private void sendThresholdAlert(UsageRequest req, PolicyDTO policy, double total, double cap,
                                     double percentage, String alertType, double thresholdLabel) {
        log.warn("ALERT: Policy {} has reached {}% of its usage cap! (Current total: {}, Cap: {})",
                req.policyId(), thresholdLabel, total, cap);
        policyServiceClient.sendUsageAlert(req.policyId(), alertType, percentage).subscribe();

        try {
            com.odmip.common.dto.UserDTO userDto = policyServiceClient.getUser(req.userId()).block();
            if (userDto != null && userDto.email() != null) {
                emailNotificationService.sendCapWarning(
                        userDto.email(),
                        policy != null ? policy.policyNumber() : String.valueOf(req.policyId()),
                        thresholdLabel);
            }
        } catch (Exception e) {
            log.warn("Could not retrieve user email for usage alert: {}", e.getMessage());
        }
    }

    public List<UsageLog> forPolicy(Long policyId) {
        return usageLogRepository.findByPolicyId(policyId);
    }

    /** Policy Utilization Analytics: total recorded usage for a policy. */
    public double totalUsage(Long policyId) {
        Double total = usageLogRepository.totalUsageForPolicy(policyId);
        return total != null ? total : 0.0;
    }
}
