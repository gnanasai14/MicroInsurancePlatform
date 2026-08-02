package com.odmip.user.controller;

import com.odmip.common.dto.ApiResponse;
import com.odmip.user.dto.TemplateRequest;
import com.odmip.user.entity.PolicyTemplate;
import com.odmip.user.service.PolicyTemplateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@Tag(name = "Policy Templates", description = "Dynamic policy product definitions")
@SecurityRequirement(name = "bearerAuth")
public class PolicyTemplateController {

    private final PolicyTemplateService templateService;

    public PolicyTemplateController(PolicyTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<PolicyTemplate>> all() {
        return ApiResponse.ok(templateService.findAll());
    }

    @GetMapping("/{code}")
    public ApiResponse<PolicyTemplate> byCode(@PathVariable String code) {
        return ApiResponse.ok(templateService.findByCode(code));
    }

    @PostMapping
    public ApiResponse<PolicyTemplate> create(@Valid @RequestBody TemplateRequest request) {
        return ApiResponse.ok("Template created", templateService.create(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<PolicyTemplate> deactivate(@PathVariable Long id) {
        return ApiResponse.ok("Template deactivated", templateService.deactivate(id));
    }
}
