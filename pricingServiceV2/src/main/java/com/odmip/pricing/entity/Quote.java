package com.odmip.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "premium_quote")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long policyId;
    private Long userId;

    @Column(nullable = false)
    private String riskCategory;

    @Column(nullable = false)
    private BigDecimal basePremium;

    @Column(nullable = false)
    private BigDecimal finalPremium;

    private String couponCode;
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
