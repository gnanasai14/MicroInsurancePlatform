package com.odmip.user.repository;

import com.odmip.user.entity.Role;
import com.odmip.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) -> username == null || username.isBlank() ? null :
                cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> email == null || email.isBlank() ? null :
                cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) -> role == null ? null :
                cb.isMember(role, root.get("roles"));
    }

    public static Specification<User> hasEnabled(Boolean enabled) {
        return (root, query, cb) -> enabled == null ? null :
                cb.equal(root.get("enabled"), enabled);
    }
}
