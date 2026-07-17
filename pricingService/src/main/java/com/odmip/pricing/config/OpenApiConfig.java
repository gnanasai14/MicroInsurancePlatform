package com.odmip.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI pricingServiceOpenAPI() {
        return new OpenAPI().info(new Info().title("Pricing, Usage & Insights API").version("v1")
                .description("Person B: Premium calculation, dynamic pricing, coupons, usage tracking, dashboard"));
    }
}
