package com.odmip.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_flag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long claimId;

    @Column(nullable = false)
    private String ruleTriggered; // e.g. "AMOUNT_EXCEEDS_COVERAGE", "MULTIPLE_CLAIMS_SHORT_WINDOW"

    @Column(nullable = false)
    private String reason;

    @Column(updatable = false)
    private LocalDateTime flaggedAt;

    @PrePersist
    void onCreate() {
        this.flaggedAt = LocalDateTime.now();
    }
}
