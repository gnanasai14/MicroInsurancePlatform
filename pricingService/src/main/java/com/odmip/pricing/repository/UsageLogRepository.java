package com.odmip.pricing.repository;

import com.odmip.pricing.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    List<UsageLog> findByPolicyId(Long policyId);
    List<UsageLog> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
        "select coalesce(sum(u.quantity), 0) from UsageLog u where u.policyId = :policyId")
    Double totalUsageForPolicy(Long policyId);
}
