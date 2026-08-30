package com.odmip.user.repository;

import com.odmip.user.entity.PolicyPremiumHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyPremiumHistoryRepository extends JpaRepository<PolicyPremiumHistory, Long> {
    List<PolicyPremiumHistory> findByPolicyIdOrderByChangedAtAsc(Long policyId);
}
