package com.odmip.user.service;

import com.odmip.common.exception.ResourceNotFoundException;
import com.odmip.user.dto.TemplateRequest;
import com.odmip.user.entity.PolicyTemplate;
import com.odmip.user.repository.PolicyTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyTemplateService {

    private final PolicyTemplateRepository templateRepository;

    public PolicyTemplateService(PolicyTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<PolicyTemplate> findAll() {
        return templateRepository.findAll();
    }

    public PolicyTemplate findByCode(String code) {
        return templateRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("No template with code " + code));
    }

    public PolicyTemplate create(TemplateRequest req) {
        PolicyTemplate template = PolicyTemplate.builder()
                .code(req.code())
                .name(req.name())
                .description(req.description())
                .baseCoverageAmount(req.baseCoverageAmount())
                .basePremium(req.basePremium())
                .defaultDurationHours(req.defaultDurationHours())
                .riskCategory(req.riskCategory())
                .usageCap(req.usageCap())
                .active(true)
                .build();
        return templateRepository.save(template);
    }

    public PolicyTemplate deactivate(Long id) {
        PolicyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No template with id " + id));
        template.setActive(false);
        return templateRepository.save(template);
    }
}
