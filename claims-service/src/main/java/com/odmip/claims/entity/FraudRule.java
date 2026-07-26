package com.odmip.claims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fraud_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleCode;

    private String description;

    @Column(nullable = false)
    private String conditionType; // AMOUNT_THRESHOLD, HIGH_CLAIM_FREQUENCY, VELOCITY_WINDOW

    private BigDecimal thresholdAmount;

    private Integer thresholdCount;

    @Column(nullable = false)
    private Integer riskScoreWeight; // e.g. +25, +50

    @Builder.Default
    private boolean active = true;
}
