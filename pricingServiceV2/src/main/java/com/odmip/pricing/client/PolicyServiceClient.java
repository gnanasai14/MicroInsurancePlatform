package com.odmip.pricing.client;

import com.odmip.common.dto.ApiResponse;
import com.odmip.common.dto.PolicyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Thin client around Person A's user-service Policy API.
 * Depends only on the shared PolicyDTO contract in `common`, never on
 * user-service's internal Policy entity - keeps B decoupled from A's
 * implementation details.
 *
 * Deliberately fails soft: if user-service isn't running yet (e.g. Person B
 * developing before A's implementation lands), calls return Mono.empty()
 * instead of blowing up, so B can keep building/testing against mocks.
 */
@Component
public class PolicyServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PolicyServiceClient.class);

    private final WebClient webClient;

    public PolicyServiceClient(WebClient.Builder builder,
                                @Value("${odmip.services.user-service-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<PolicyDTO> getPolicy(Long policyId) {
        return webClient.get()
                .uri("/api/policies/{id}", policyId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponse<PolicyDTO>>() {})
                .map(ApiResponse::data)
                .onErrorResume(ex -> {
                    log.warn("Could not reach user-service for policy {}: {}", policyId, ex.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<java.util.List<PolicyDTO>> getPoliciesByUserId(Long userId) {
        return webClient.get()
                .uri("/api/policies/user/{userId}", userId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<ApiResponse<java.util.List<PolicyDTO>>>() {})
                .map(ApiResponse::data)
                .onErrorResume(ex -> {
                    log.warn("Could not reach user-service for user policies {}: {}", userId, ex.getMessage());
                    return Mono.just(java.util.Collections.emptyList());
                });
    }

    public Mono<Void> updatePolicyPremium(Long policyId, java.math.BigDecimal premium) {
        return webClient.patch()
                .uri("/api/policies/{id}", policyId)
                .bodyValue(java.util.Map.of("premium", premium))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> {
                    log.warn("Could not persist premium to user-service for policy {}: {}", policyId, ex.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Void> sendUsageAlert(Long policyId, String alertType, double percentage) {
        return webClient.post()
                .uri("/api/policies/{id}/usage-alert", policyId)
                .bodyValue(java.util.Map.of("alertType", alertType, "percentage", percentage))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(ex -> {
                    log.warn("Could not send usage alert to user-service for policy {}: {}", policyId, ex.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<java.util.List<com.odmip.pricing.dto.PremiumHistoryDTO>> getPremiumHistory(Long policyId) {
        return webClient.get()
                .uri("/api/policies/{id}/premium-history", policyId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<java.util.List<com.odmip.pricing.dto.PremiumHistoryDTO>>>() {})
                .map(com.odmip.common.dto.ApiResponse::data)
                .onErrorResume(ex -> {
                    log.warn("Could not fetch premium history for policy {}: {}", policyId, ex.getMessage());
                    return Mono.just(java.util.Collections.emptyList());
                });
    }

    public Mono<com.odmip.common.dto.UserDTO> getUser(Long userId) {
        return webClient.get()
                .uri("/api/auth/users/{id}", userId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<com.odmip.common.dto.ApiResponse<com.odmip.common.dto.UserDTO>>() {})
                .map(com.odmip.common.dto.ApiResponse::data)
                .onErrorResume(ex -> {
                    log.warn("Could not fetch user info for userId {}: {}", userId, ex.getMessage());
                    return Mono.empty();
                });
    }
}
