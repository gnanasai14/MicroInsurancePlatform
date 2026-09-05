package com.odmip.pricing.config;

import com.odmip.pricing.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * pricing-service previously had no authentication at all - every endpoint,
 * including pricing-rule admin CRUD and coupon creation, was open to anyone
 * who could reach port 8082. This validates the same JWT user-service issues
 * (shared HMAC secret, app.jwt.secret - must match user-service's value in
 * both environments) rather than re-implementing login here.
 *
 *  - /swagger-ui/**, /v3/api-docs/**, /h2-console/**  -> public (dev tooling)
 *  - write endpoints on pricing rules & coupons        -> ADMIN only
 *  - everything else                                   -> any authenticated user
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${odmip.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /** Allows the React frontend (a different origin/port) to call this API with an Authorization header. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pricing/rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/pricing/rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/pricing/rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/coupons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pricing/quote/accepted").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // needed for h2-console

        return http.build();
    }
}
