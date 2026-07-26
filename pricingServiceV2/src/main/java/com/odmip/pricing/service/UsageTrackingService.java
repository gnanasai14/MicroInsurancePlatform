package com.odmip.pricing.service;

import com.odmip.common.dto.PolicyDTO;
import com.odmip.pricing.client.PolicyServiceClient;
import com.odmip.pricing.dto.UsageRequest;
import com.odmip.pricing.entity.UsageLog;
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

    public UsageTrackingService(UsageLogRepository usageLogRepository, PolicyServiceClient policyServiceClient) {
        this.usageLogRepository = usageLogRepository;
        this.policyServiceClient = policyServiceClient;
    }

    public UsageLog record(UsageRequest req) {
        UsageLog usage = UsageLog.builder()
                .policyId(req.policyId())
                .userId(req.userId())
                .usageType(req.usageType())
                .quantity(req.quantity())
                .build();
        UsageLog saved = usageLogRepository.save(usage);

        // Threshold detection (80% / 100% of usage cap)
        try {
            double total = totalUsage(req.policyId());
            PolicyDTO policy = policyServiceClient.getPolicy(req.policyId()).block();
            double cap = (policy != null && policy.usageCap() != null) ? policy.usageCap() : 100.0; // default cap is 100.0

            double percentage = (total / cap) * 100.0;
            if (percentage >= 100.0) {
                log.warn("ALERT: Policy {} has reached 100% of its usage cap! (Current total: {}, Cap: {})", req.policyId(), total, cap);
                policyServiceClient.sendUsageAlert(req.policyId(), "CAP_REACHED", percentage).subscribe();
            } else if (percentage >= 80.0) {
                log.warn("ALERT: Policy {} has reached 80% of its usage cap! (Current total: {}, Cap: {})", req.policyId(), total, cap);
                policyServiceClient.sendUsageAlert(req.policyId(), "WARNING_80_PERCENT", percentage).subscribe();
            }
        } catch (Exception ex) {
            log.warn("Failed to check usage threshold alert for policy {}: {}", req.policyId(), ex.getMessage());
        }

        return saved;
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
