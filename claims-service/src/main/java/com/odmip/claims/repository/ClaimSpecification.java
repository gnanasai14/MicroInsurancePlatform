package com.odmip.claims.repository;

import com.odmip.claims.entity.Claim;
import com.odmip.claims.entity.ClaimStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Was imported by ClaimsAnalyticsService but never actually existed in the
 * codebase - claims-service could never have compiled as originally
 * uploaded. ClaimRepository already extends JpaSpecificationExecutor<Claim>
 * (that part was intact), it just had nothing implementing Specification<Claim>
 * to hand it. Each method returns null when its filter argument is null,
 * which Specification.where(...).and(...) treats as "no additional
 * constraint" - exactly what ClaimsAnalyticsService's optional
 * status/startDate/endDate filters need.
 */
public class ClaimSpecification {

    private ClaimSpecification() {}

    public static Specification<Claim> hasStatus(ClaimStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Claim> submittedAfter(LocalDateTime start) {
        return (root, query, cb) -> start == null ? null : cb.greaterThanOrEqualTo(root.get("submittedAt"), start);
    }

    public static Specification<Claim> submittedBefore(LocalDateTime end) {
        return (root, query, cb) -> end == null ? null : cb.lessThanOrEqualTo(root.get("submittedAt"), end);
    }
}
