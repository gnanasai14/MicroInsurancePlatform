package com.odmip.claims.repository;

import com.odmip.claims.entity.NotificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, Long> {
    List<NotificationAudit> findByClaimId(Long claimId);
}
