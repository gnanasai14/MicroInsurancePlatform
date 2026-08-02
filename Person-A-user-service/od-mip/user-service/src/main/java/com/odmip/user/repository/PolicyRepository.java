package com.odmip.user.repository;

import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByUserId(Long userId);
    List<Policy> findByStatusAndEndDateBefore(PolicyStatus status, LocalDateTime cutoff);
}
