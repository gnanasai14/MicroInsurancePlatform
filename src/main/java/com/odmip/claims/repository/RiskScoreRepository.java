package com.odmip.claims.repository;

import com.odmip.claims.entity.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {
    Optional<RiskScore> findByClaimId(Long claimId);
}
