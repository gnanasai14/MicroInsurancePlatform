package com.odmip.user.service;

import com.odmip.common.exception.BusinessRuleException;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.user.dto.PolicyCreateRequest;
import com.odmip.user.dto.PolicyPatchRequest;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyPremiumHistory;
import com.odmip.user.entity.PolicyStatus;
import com.odmip.user.entity.PolicyTemplate;
import com.odmip.user.repository.PolicyPremiumHistoryRepository;
import com.odmip.user.repository.PolicyRepository;
import com.odmip.user.repository.PolicyTemplateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyTemplateRepository templateRepository;
    private final PolicyPremiumHistoryRepository premiumHistoryRepository;

    public PolicyService(PolicyRepository policyRepository, PolicyTemplateRepository templateRepository,
                         PolicyPremiumHistoryRepository premiumHistoryRepository) {
        this.policyRepository = policyRepository;
        this.templateRepository = templateRepository;
        this.premiumHistoryRepository = premiumHistoryRepository;
    }

    /** Creates an on-demand policy from a template. Starts life as DRAFT; activated separately. */
    public Policy create(PolicyCreateRequest req) {
        PolicyTemplate template = templateRepository.findByCode(req.templateCode())
                .orElseThrow(() -> new ResourceNotFoundException("No template with code " + req.templateCode()));

        if (!template.isActive()) {
            throw new BusinessRuleException("Template " + req.templateCode() + " is not active");
        }

        int hours = req.durationHoursOverride() != null ? req.durationHoursOverride() : template.getDefaultDurationHours();
        LocalDateTime start = LocalDateTime.now();

        Policy policy = Policy.builder()
                .policyNumber(generatePolicyNumber())
                .userId(req.userId())
                .template(template)
                .status(PolicyStatus.DRAFT)
                .coverageAmount(template.getBaseCoverageAmount())
                .premiumAmount(template.getBasePremium()) // placeholder until pricing-service overrides it
                .startDate(start)
                .endDate(start.plusHours(hours))
                .build();

        Policy savedPolicy = policyRepository.save(policy);

        // Record initial premium in history
        PolicyPremiumHistory history = PolicyPremiumHistory.builder()
                .policyId(savedPolicy.getId())
                .premiumAmount(savedPolicy.getPremiumAmount())
                .changedAt(LocalDateTime.now())
                .build();
        premiumHistoryRepository.save(history);

        return savedPolicy;
    }

    public Policy activate(Long policyId) {
        Policy policy = getById(policyId);
        validateStatusTransition(policy, PolicyStatus.ACTIVE);
        policy.setStatus(PolicyStatus.ACTIVE);
        return policyRepository.save(policy);
    }

    public Policy cancel(Long policyId) {
        Policy policy = getById(policyId);
        validateStatusTransition(policy, PolicyStatus.CANCELLED);
        policy.setStatus(PolicyStatus.CANCELLED);
        return policyRepository.save(policy);
    }

    public void validateStatusTransition(Policy policy, PolicyStatus targetStatus) {
        PolicyStatus currentStatus = policy.getStatus();
        if (currentStatus == targetStatus) {
            return;
        }

        if (currentStatus == PolicyStatus.EXPIRED) {
            if (targetStatus == PolicyStatus.CANCELLED) {
                throw new BusinessRuleException("Cannot cancel an already-expired policy");
            }
            throw new BusinessRuleException("Cannot change status of an expired policy");
        }

        if (targetStatus == PolicyStatus.ACTIVE) {
            if (currentStatus != PolicyStatus.DRAFT) {
                throw new BusinessRuleException("Only DRAFT policies can be activated (current: " + currentStatus + ")");
            }
        } else if (targetStatus == PolicyStatus.CANCELLED) {
            if (currentStatus == PolicyStatus.EXPIRED) {
                throw new BusinessRuleException("Cannot cancel an already-expired policy");
            }
        } else if (targetStatus == PolicyStatus.EXPIRED) {
            if (LocalDateTime.now().isBefore(policy.getEndDate())) {
                throw new BusinessRuleException("Cannot expire a policy before its end date");
            }
        }
    }

    public Policy patchPolicy(Long id, PolicyPatchRequest req) {
        Policy policy = getById(id);

        if (req.premium() != null) {
            policy.setPremiumAmount(req.premium());
            // Record premium update in history
            PolicyPremiumHistory history = PolicyPremiumHistory.builder()
                    .policyId(policy.getId())
                    .premiumAmount(req.premium())
                    .changedAt(LocalDateTime.now())
                    .build();
            premiumHistoryRepository.save(history);
        }

        if (req.status() != null) {
            validateStatusTransition(policy, req.status());
            policy.setStatus(req.status());
        }

        return policyRepository.save(policy);
    }

    public List<PolicyPremiumHistory> getPremiumHistory(Long policyId) {
        getById(policyId);
        return premiumHistoryRepository.findByPolicyIdOrderByChangedAtAsc(policyId);
    }

    public Policy getById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No policy with id " + id));
    }

    public List<Policy> findByUser(Long userId) {
        return policyRepository.findByUserId(userId);
    }

    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    public org.springframework.data.domain.Page<Policy> findAll(org.springframework.data.jpa.domain.Specification<Policy> spec, org.springframework.data.domain.Pageable pageable) {
        return policyRepository.findAll(spec, pageable);
    }

    private String generatePolicyNumber() {
        return "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
