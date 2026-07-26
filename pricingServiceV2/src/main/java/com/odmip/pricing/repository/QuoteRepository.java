package com.odmip.pricing.repository;

import com.odmip.pricing.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Optional<Quote> findByPolicyId(Long policyId);

    @Query("SELECT q.riskCategory, SUM(q.finalPremium) FROM Quote q GROUP BY q.riskCategory")
    List<Object[]> aggregateRevenueByRiskCategory();
}
