package com.odmip.claims.client;

import com.odmip.common.exception.UserServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PolicyServiceClient {

    private final WebClient webClient;

    public PolicyServiceClient(WebClient.Builder builder,
                                @Value("${odmip.services.user-service-url:http://localhost:8081}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public List<Map<String, Object>> getUserPolicies(Long userId) {
        try {
            List<Map<String, Object>> policies = webClient.get()
                    .uri("/api/policies/user/{userId}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            return policies != null ? policies : Collections.emptyList();
        } catch (WebClientResponseException ex) {
            log.warn("WebClient response exception from user-service for user {}: {}", userId, ex.getMessage());
            if (ex.getStatusCode().is5xxServerError()) {
                throw new UserServiceUnavailableException("User service returned an error", ex);
            }
            if (ex.getStatusCode().value() == 404) {
                return Collections.emptyList();
            }
            throw ex;
        } catch (WebClientRequestException ex) {
            log.warn("Connection/Request exception from user-service for user {}: {}", userId, ex.getMessage());
            throw new UserServiceUnavailableException("User service is temporarily unavailable", ex);
        } catch (Exception ex) {
            log.warn("Unexpected exception from user-service for user {}: {}", userId, ex.getMessage());
            throw new UserServiceUnavailableException("User service call failed", ex);
        }
    }

    public Map<String, Object> getPolicy(Long policyId) {
        try {
            return webClient.get()
                    .uri("/api/policies/{id}", policyId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("WebClient response exception from user-service for policy {}: {}", policyId, ex.getMessage());
            if (ex.getStatusCode().is5xxServerError()) {
                throw new UserServiceUnavailableException("User service returned an error", ex);
            }
            if (ex.getStatusCode().value() == 404) {
                return null;
            }
            throw ex;
        } catch (WebClientRequestException ex) {
            log.warn("Connection/Request exception from user-service for policy {}: {}", policyId, ex.getMessage());
            throw new UserServiceUnavailableException("User service is temporarily unavailable", ex);
        } catch (Exception ex) {
            log.warn("Unexpected exception from user-service for policy {}: {}", policyId, ex.getMessage());
            throw new UserServiceUnavailableException("User service call failed", ex);
        }
    }

    public boolean isPolicyActive(Long policyId, Long userId) {
        List<Map<String, Object>> policies = getUserPolicies(userId);
        if (policies.isEmpty()) {
            return false;
        }
        return policies.stream()
                .anyMatch(p -> {
                    Object id = p.get("id");
                    Object status = p.get("status");
                    return id != null && policyId.toString().equals(id.toString())
                            && status != null && "ACTIVE".equalsIgnoreCase(status.toString());
                });
    }
}
