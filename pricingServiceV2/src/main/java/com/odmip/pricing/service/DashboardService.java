package com.odmip.pricing.service;

import com.odmip.common.dto.PolicyDTO;
import com.odmip.pricing.client.PolicyServiceClient;
import com.odmip.pricing.dto.DashboardSummary;
import com.odmip.pricing.repository.UsageLogRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

/**
 * User Dashboard aggregation. Currently accepts a caller-supplied list of
 * policyIds for the user (frontend gets these from user-service first) and
 * enriches them with usage + a mock premium total. Week 3+: replace with a
 * single user-service call once GET /api/policies/user/{id} response shape
 * is finalized with Person A.
 */
@Service
public class DashboardService {

    private final PolicyServiceClient policyServiceClient;
    private final UsageLogRepository usageLogRepository;

    public DashboardService(PolicyServiceClient policyServiceClient, UsageLogRepository usageLogRepository) {
        this.policyServiceClient = policyServiceClient;
        this.usageLogRepository = usageLogRepository;
    }

    public DashboardSummary summarize(Long userId) {
        List<PolicyDTO> policies = policyServiceClient.getPoliciesByUserId(userId).block();

        int active = 0;
        BigDecimal totalPremium = BigDecimal.ZERO;
        var entries = new java.util.ArrayList<DashboardSummary.PolicyUsageEntry>();

        int totalPoliciesCount = 0;
        if (policies != null) {
            totalPoliciesCount = policies.size();
            for (PolicyDTO p : policies) {
                if ("ACTIVE".equals(p.status())) active++;
                Double totalUsage = usageLogRepository.totalUsageForPolicy(p.id());
                entries.add(new DashboardSummary.PolicyUsageEntry(
                        p.id(), p.policyNumber(), p.status(), totalUsage != null ? totalUsage : 0.0));

                List<com.odmip.pricing.dto.PremiumHistoryDTO> history = policyServiceClient.getPremiumHistory(p.id()).block();
                if (history != null && !history.isEmpty()) {
                    BigDecimal policyPremium = history.get(history.size() - 1).premiumAmount();
                    if (policyPremium != null) {
                        totalPremium = totalPremium.add(policyPremium);
                    }
                }
            }
        }

        return new DashboardSummary(userId, active, totalPoliciesCount, totalPremium, entries);
    }
}
