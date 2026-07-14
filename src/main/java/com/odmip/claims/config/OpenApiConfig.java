package com.odmip.claims.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI claimsServiceOpenAPI() {
        return new OpenAPI().info(new Info().title("Claims, Risk & Notifications API").version("v1")
                .description("Person C: Claim submission/validation/status tracking, fraud detection, risk scoring, SNS alerts"));
    }
}
