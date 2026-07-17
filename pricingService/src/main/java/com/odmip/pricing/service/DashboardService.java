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

    public DashboardSummary summarize(Long userId, List<Long> policyIds) {
        List<PolicyDTO> policies = Flux.fromIterable(policyIds)
                .flatMap(policyServiceClient::getPolicy)
                .collectList()
                .block(); // simple blocking call is fine for a dashboard read - not on a hot path

        int active = 0;
        BigDecimal totalPremium = BigDecimal.ZERO;
        var entries = new java.util.ArrayList<DashboardSummary.PolicyUsageEntry>();

        if (policies != null) {
            for (PolicyDTO p : policies) {
                if ("ACTIVE".equals(p.status())) active++;
                Double totalUsage = usageLogRepository.totalUsageForPolicy(p.id());
                entries.add(new DashboardSummary.PolicyUsageEntry(
                        p.id(), p.policyNumber(), p.status(), totalUsage != null ? totalUsage : 0.0));
            }
        }

        return new DashboardSummary(userId, active, policyIds.size(), totalPremium, entries);
    }
}
