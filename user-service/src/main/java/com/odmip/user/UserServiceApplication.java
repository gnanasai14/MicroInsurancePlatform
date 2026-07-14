package com.odmip.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PERSON A - Foundation & Policy Core
 * Runs standalone on port 8081.
 *   mvn -pl user-service -am spring-boot:run
 * Swagger UI: http://localhost:8081/swagger-ui.html
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.odmip") // pulls in shared GlobalExceptionHandler from `common`
@EnableScheduling
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
