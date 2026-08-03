package com.odmip.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumAmount;

    @Column(nullable = false)
    private LocalDateTime changedAt;
}
