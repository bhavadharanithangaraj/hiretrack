package com.hiretrack.config;

import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;
import com.hiretrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Runs once when the app starts (CommandLineRunner.run() is called after Spring is ready).
// Creates the admin account if it doesn't exist yet. Credentials come from application.properties.
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.admin.email}") String adminEmail,
                      @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(new User("Admin", adminEmail, passwordEncoder.encode(adminPassword), Role.ROLE_ADMIN));
            log.info("Seeded admin user {}", adminEmail);
        }
    }
}
