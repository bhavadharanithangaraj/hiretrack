package com.hiretrack.config;

import com.hiretrack.entity.Role;
import com.hiretrack.entity.User;
import com.hiretrack.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Runs once when the app starts. CommandLineRunner.run() is called after Spring is ready.
// TODO (Step 3): seed the admin user here instead, with a BCrypt-hashed password.
@Component
public class DataSeeder implements CommandLineRunner {

    public static final String DEMO_USER_EMAIL = "demo@hiretrack.com";

    private final UserRepository userRepository;

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(DEMO_USER_EMAIL)) {
            // "!" is not a valid BCrypt hash, so nobody can ever log in as this placeholder user
            userRepository.save(new User("Demo User", DEMO_USER_EMAIL, "!", Role.ROLE_USER));
        }
    }
}
