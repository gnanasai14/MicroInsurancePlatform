package com.odmip.user.service;

import com.odmip.common.dto.PolicyDTO;
import com.odmip.common.exception.BusinessRuleException;
import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.user.dto.PolicyCreateRequest;
import com.odmip.user.dto.PolicyPatchRequest;
import com.odmip.user.entity.Policy;
import com.odmip.user.entity.PolicyStatus;
import com.odmip.user.entity.PolicyTemplate;
import com.odmip.user.repository.PolicyRepository;
import com.odmip.user.repository.PolicyTemplateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyTemplateRepository templateRepository;

    public PolicyService(PolicyRepository policyRepository, PolicyTemplateRepository templateRepository) {
        this.policyRepository = policyRepository;
        this.templateRepository = templateRepository;
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

        return policyRepository.save(policy);
    }

    public Policy activate(Long policyId) {
        Policy policy = getById(policyId);
        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT policies can be activated (current: " + policy.getStatus() + ")");
        }
        policy.setStatus(PolicyStatus.ACTIVE);
        return policyRepository.save(policy);
    }

    public Policy cancel(Long policyId) {
        Policy policy = getById(policyId);
        if (policy.getStatus() == PolicyStatus.EXPIRED) {
            throw new BusinessRuleException("Cannot cancel an already-expired policy");
        }
        policy.setStatus(PolicyStatus.CANCELLED);
        return policyRepository.save(policy);
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

    public Policy patch(Long id, PolicyPatchRequest req) {
        Policy policy = getById(id);
        if (req.status() != null) {
            policy.setStatus(PolicyStatus.valueOf(req.status().toUpperCase()));
        }
        if (req.premiumAmount() != null) {
            policy.setPremiumAmount(req.premiumAmount());
        }
        return policyRepository.save(policy);
    }

    public PolicyDTO mapToDTO(Policy policy) {
        return new PolicyDTO(
                policy.getId(),
                policy.getUserId(),
                policy.getPolicyNumber(),
                policy.getTemplate().getCode(),
                policy.getStatus().name(),
                policy.getCoverageAmount(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getTemplate().getRiskCategory()
        );
    }

    private String generatePolicyNumber() {
        return "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
