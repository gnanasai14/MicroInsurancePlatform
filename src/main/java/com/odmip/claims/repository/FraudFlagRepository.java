package com.odmip.claims.repository;

import com.odmip.claims.entity.FraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudFlagRepository extends JpaRepository<FraudFlag, Long> {
    List<FraudFlag> findByClaimId(Long claimId);
}
