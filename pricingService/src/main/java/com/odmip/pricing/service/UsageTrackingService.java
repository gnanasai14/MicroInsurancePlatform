package com.odmip.pricing.service;

import com.odmip.pricing.dto.UsageRequest;
import com.odmip.pricing.entity.UsageLog;
import com.odmip.pricing.repository.UsageLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsageTrackingService {

    private final UsageLogRepository usageLogRepository;

    public UsageTrackingService(UsageLogRepository usageLogRepository) {
        this.usageLogRepository = usageLogRepository;
    }

    public UsageLog record(UsageRequest req) {
        UsageLog log = UsageLog.builder()
                .policyId(req.policyId())
                .userId(req.userId())
                .usageType(req.usageType())
                .quantity(req.quantity())
                .build();
        return usageLogRepository.save(log);
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
