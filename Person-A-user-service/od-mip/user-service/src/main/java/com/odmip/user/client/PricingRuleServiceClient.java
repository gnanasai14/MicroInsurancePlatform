package com.odmip.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Now that pricing-service enforces auth, write operations here need to
 * forward the calling admin's own JWT rather than calling anonymously -
 * pricing-service has no way to tell "this is a trusted internal call" apart
 * from "this is any other client" otherwise, and its ROLE_ADMIN check on
 * writes would reject an unauthenticated request the same as anyone else's.
 * AdminController passes the incoming request's Authorization header through
 * to these methods; reads are still unauthenticated-compatible on pricing's
 * side but we forward the token there too when we have it, for consistency.
 */
@Component
public class PricingRuleServiceClient {

    private final RestClient restClient;

    public PricingRuleServiceClient(@Value("${odmip.services.pricing-service-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<Map<String, Object>> getAllRules(String bearerToken) {
        return restClient.get()
                .uri("/api/pricing/rules")
                .headers(h -> addAuth(h, bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<List<Map<String, Object>>>>() {})
                .data();
    }

    public Map<String, Object> getRuleById(Long id, String bearerToken) {
        return restClient.get()
                .uri("/api/pricing/rules/{id}", id)
                .headers(h -> addAuth(h, bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<Map<String, Object>>>() {})
                .data();
    }

    public Map<String, Object> createRule(Map<String, Object> rule, String bearerToken) {
        return restClient.post()
                .uri("/api/pricing/rules")
                .headers(h -> addAuth(h, bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(rule)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<Map<String, Object>>>() {})
                .data();
    }

    public Map<String, Object> updateRule(Long id, Map<String, Object> rule, String bearerToken) {
        return restClient.put()
                .uri("/api/pricing/rules/{id}", id)
                .headers(h -> addAuth(h, bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(rule)
                .retrieve()
                .body(new ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<Map<String, Object>>>() {})
                .data();
    }

    public void deleteRule(Long id, String bearerToken) {
        restClient.delete()
                .uri("/api/pricing/rules/{id}", id)
                .headers(h -> addAuth(h, bearerToken))
                .retrieve()
                .toBodilessEntity();
    }

    private void addAuth(HttpHeaders headers, String bearerToken) {
        if (bearerToken != null && !bearerToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        }
    }
}
