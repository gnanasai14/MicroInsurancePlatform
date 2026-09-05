package com.odmip.user.config;

import com.odmip.user.entity.PolicyTemplate;
import com.odmip.user.entity.Role;
import com.odmip.user.entity.User;
import com.odmip.user.repository.PolicyTemplateRepository;
import com.odmip.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/** Seeds a default admin + a couple of policy templates so B/C can start against real data immediately. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PolicyTemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PolicyTemplateRepository templateRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .username("admin")
                    .email("admin@odmip.local")
                    .sms("+15555551234")
                    .password(passwordEncoder.encode("Admin@123"))
                    .enabled(true)
                    .emailVerified(true)
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                    .build());
        }

        if (templateRepository.findByCode("TRAVEL_1DAY").isEmpty()) {
            templateRepository.save(PolicyTemplate.builder()
                    .code("TRAVEL_1DAY")
                    .name("1-Day Travel Cover")
                    .description("Short-term travel accident + baggage cover")
                    .baseCoverageAmount(new BigDecimal("5000"))
                    .basePremium(new BigDecimal("2.50")) // (5000/1000) x LOW(1.0) x (24h/24) x 0.5 rate
                    .defaultDurationHours(24)
                    .riskCategory("LOW")
                    .usageCap(24.0)
                    .active(true)
                    .build());
        }

        if (templateRepository.findByCode("BIKE_WEEKEND").isEmpty()) {
            templateRepository.save(PolicyTemplate.builder()
                    .code("BIKE_WEEKEND")
                    .name("Weekend Bike Insurance")
                    .description("Theft + damage cover for weekend rides")
                    .baseCoverageAmount(new BigDecimal("2000"))
                    .basePremium(new BigDecimal("3.00")) // (2000/1000) x MEDIUM(1.5) x (48h/24) x 0.5 rate
                    .defaultDurationHours(48)
                    .riskCategory("MEDIUM")
                    .usageCap(48.0)
                    .active(true)
                    .build());
        }
    }
}
