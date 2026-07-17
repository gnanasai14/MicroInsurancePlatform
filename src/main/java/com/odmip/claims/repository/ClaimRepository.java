package com.odmip.claims.repository;

import com.odmip.claims.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicyId(Long policyId);
    List<Claim> findByUserId(Long userId);
    List<Claim> findByUserIdAndSubmittedAtAfter(Long userId, LocalDateTime since);
}
