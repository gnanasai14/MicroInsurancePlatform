package com.odmip.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_score")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long claimId;

    @Column(nullable = false)
    private int score; // 0-100, higher = riskier

    @Column(nullable = false)
    private String tier; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(updatable = false)
    private LocalDateTime scoredAt;

    @PrePersist
    void onCreate() {
        this.scoredAt = LocalDateTime.now();
    }
}
