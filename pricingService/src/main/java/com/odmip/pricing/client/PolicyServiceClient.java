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
}
