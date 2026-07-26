package com.odmip.claims.repository;

import com.odmip.claims.entity.ClaimValidationRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClaimValidationRetryRepository extends JpaRepository<ClaimValidationRetry, Long> {
    List<ClaimValidationRetry> findByStatusAndNextRetryAtBefore(String status, LocalDateTime time);
}
