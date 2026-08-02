package com.odmip.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A reusable "product definition" (e.g. "1-Day Travel Cover", "Weekend Bike
 * Insurance") that a Policy is instantiated from. This is what makes policy
 * creation "dynamic" instead of hardcoded per product.
 */
@Entity
@Table(name = "policy_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;               // e.g. "TRAVEL_1DAY"

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal baseCoverageAmount;

    @Column(nullable = false)
    private BigDecimal basePremium;

    @Column(nullable = false)
    private Integer defaultDurationHours;

    @Column(nullable = false)
    private String riskCategory;       // LOW, MEDIUM, HIGH - drives pricing/risk

    @Column(nullable = false)
    private boolean active;
}
