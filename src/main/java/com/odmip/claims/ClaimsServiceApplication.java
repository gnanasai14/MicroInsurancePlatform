package com.odmip.claims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Claims, Risk & Notifications
 * Runs standalone on port 8083.
 *   mvn -pl claims-service -am spring-boot:run
 * Swagger UI: http://localhost:8083/swagger-ui.html
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.odmip") // pulls in shared GlobalExceptionHandler from `common`
public class ClaimsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClaimsServiceApplication.class, args);
    }
}
