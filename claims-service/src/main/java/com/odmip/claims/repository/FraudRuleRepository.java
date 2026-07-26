package com.odmip.claims.repository;

import com.odmip.claims.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {
    List<FraudRule> findByActiveTrue();
    Optional<FraudRule> findByRuleCode(String ruleCode);
}
