package com.odmip.pricing.repository;

import com.odmip.pricing.entity.PolicyAlertState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyAlertStateRepository extends JpaRepository<PolicyAlertState, Long> {
}
