package com.odmip.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "policy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String policyNumber;

    @Column(nullable = false)
    private Long userId;

    // FIX: changed from FetchType.LAZY to FetchType.EAGER.
    // With LAZY, any Policy loaded via findById() (e.g. in activate()/cancel())
    // keeps `template` as an uninitialized Hibernate proxy. Jackson can't
    // serialize that proxy (ByteBuddyInterceptor) once the JSON response is
    // built outside the Hibernate session, causing a 500 error.
    // create() worked before because it assigned a freshly-queried, fully
    // loaded PolicyTemplate directly - never touching the lazy-loading path.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    private PolicyTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(nullable = false)
    private BigDecimal coverageAmount;

    /** Filled in by pricing-service via callback/manual update for now (week 3+: event-driven). */
    private BigDecimal premiumAmount;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = PolicyStatus.DRAFT;
    }
}