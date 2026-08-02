package com.odmip.user.repository;

import com.odmip.user.entity.PolicyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyTemplateRepository extends JpaRepository<PolicyTemplate, Long> {
    Optional<PolicyTemplate> findByCode(String code);
}
