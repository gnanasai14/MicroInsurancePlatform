package com.odmip.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class PricingRuleServiceClient {

    private final RestClient restClient;

    public PricingRuleServiceClient(@Value("${odmip.services.pricing-service-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<Map<String, Object>> getAllRules() {
        return restClient.get()
                .uri("/api/pricing/rules")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<List<Map<String, Object>>>>() {})
                .data();
    }

    public Map<String, Object> getRuleById(Long id) {
        return restClient.get()
                .uri("/api/pricing/rules/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<Map<String, Object>>>() {})
                .data();
    }

    public Map<String, Object> createRule(Map<String, Object> rule) {
        return restClient.post()
                .uri("/api/pricing/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .body(rule)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<Map<String, Object>>>() {})
                .data();
    }

    public Map<String, Object> updateRule(Long id, Map<String, Object> rule) {
        return restClient.put()
                .uri("/api/pricing/rules/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(rule)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<Map<String, Object>>>() {})
                .data();
    }

    public void deleteRule(Long id) {
        restClient.delete()
                .uri("/api/pricing/rules/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
