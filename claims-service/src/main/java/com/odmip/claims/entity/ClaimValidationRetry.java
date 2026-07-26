package com.odmip.claims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "claim_validation_retry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimValidationRetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long claimId;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private Long userId;

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED

    private LocalDateTime nextRetryAt;

    @Column(length = 1000)
    private String lastError;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.nextRetryAt == null) {
            this.nextRetryAt = this.createdAt;
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
