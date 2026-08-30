package com.odmip.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tracks which usage-cap thresholds have already been alerted on for a
 * policy, so UsageTrackingService only fires each threshold once per
 * crossing instead of re-alerting on every subsequent usage record.
 * Resets are intentionally NOT automatic - a policy renewal/reactivation
 * flow should explicitly clear this if usage cap tracking needs to restart.
 */
@Entity
@Table(name = "policy_alert_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyAlertState {

    @Id
    private Long policyId;

    @Column(nullable = false)
    @Builder.Default
    private boolean warning80Sent = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean capReachedSent = false;
}
