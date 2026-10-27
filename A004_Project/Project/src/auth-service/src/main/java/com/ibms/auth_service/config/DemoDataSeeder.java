package com.ibms.auth_service.config;

import com.ibms.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@ConditionalOnProperty(prefix = "app.demo-data", name = "enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, JdbcTemplate jdbcTemplate,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsById(1L)) {
            userRepository.findById(1L)
                    .filter(user -> user.getPassword() == null || user.getPassword().isBlank())
                    .ifPresent(user -> jdbcTemplate.update(
                            "UPDATE users SET password = ? WHERE id = ?",
                            passwordEncoder.encode("DemoPassword123!"),
                            user.getId()
                    ));
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO users (id, name, email, password, phone) VALUES (?, ?, ?, ?, ?)",
                1L,
                "Demo User",
                "demo@example.com",
                passwordEncoder.encode("DemoPassword123!"),
                "555-0100"
        );
    }
}
