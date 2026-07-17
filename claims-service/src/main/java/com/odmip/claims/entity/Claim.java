package com.odmip.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String claimNumber;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal claimedAmount;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime lastUpdatedAt;

    @PrePersist
    void onCreate() {
        this.submittedAt = LocalDateTime.now();
        this.lastUpdatedAt = this.submittedAt;
        if (this.status == null) this.status = ClaimStatus.SUBMITTED;
    }

    @PreUpdate
    void onUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
