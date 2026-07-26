package com.odmip.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * PERSON B - Pricing, Usage & Insights
 * Runs standalone on port 8082.
 *   mvn -pl pricing-service -am spring-boot:run
 * Swagger UI: http://localhost:8082/swagger-ui.html
 */
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = "com.odmip") // pulls in shared GlobalExceptionHandler from `common`
public class PricingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
    }
}
