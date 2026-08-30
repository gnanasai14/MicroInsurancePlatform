package com.odmip.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit trail of every premium value a Policy has had. Written to whenever
 * pricing-service (or an admin) sets a new premium via PATCH /api/policies/{id}.
 * Consumed by pricing-service's DashboardService via GET /{id}/premium-history
 * to compute a user's total premium paid.
 */
@Entity
@Table(name = "policy_premium_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPremiumHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private BigDecimal premiumAmount;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    void onCreate() {
        if (this.changedAt == null) this.changedAt = LocalDateTime.now();
    }
}
