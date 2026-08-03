package com.odmip.user.repository;

import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import org.springframework.data.jpa.domain.Specification;

public class PolicySpecification {
    public static Specification<Policy> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null :
                cb.equal(root.get("userId"), userId);
    }

    public static Specification<Policy> hasStatus(PolicyStatus status) {
        return (root, query, cb) -> status == null ? null :
                cb.equal(root.get("status"), status);
    }

    public static Specification<Policy> hasPolicyNumber(String policyNumber) {
        return (root, query, cb) -> policyNumber == null || policyNumber.isBlank() ? null :
                cb.like(cb.lower(root.get("policyNumber")), "%" + policyNumber.toLowerCase() + "%");
    }

    public static Specification<Policy> hasTemplateCode(String templateCode) {
        return (root, query, cb) -> templateCode == null || templateCode.isBlank() ? null :
                cb.equal(root.get("template").get("code"), templateCode);
    }
}
