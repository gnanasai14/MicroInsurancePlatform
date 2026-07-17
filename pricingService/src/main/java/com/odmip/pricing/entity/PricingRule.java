package com.odmip.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single multiplier/adjustment rule feeding the Dynamic Pricing Engine.
 * e.g. riskCategory=HIGH -> multiplier 1.5, location=URBAN -> multiplier 1.1
 */
@Entity
@Table(name = "pricing_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type;         // RISK, LOCATION, USAGE

    @Column(nullable = false)
    private String matchValue;     // e.g. "HIGH", "URBAN", "HEAVY"

    @Column(nullable = false)
    private BigDecimal multiplier; // applied to base premium, e.g. 1.25

    @Column(nullable = false)
    private boolean active;
}
