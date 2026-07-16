package com.odmip.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One row per "usage tick" reported for an active policy (e.g. a ride, a trip-day, a session). */
@Entity
@Table(name = "usage_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String usageType;   // e.g. "TRIP", "SESSION", "MILEAGE"

    @Column(nullable = false)
    private Double quantity;    // e.g. km driven, hours used

    @Column(nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }
}
