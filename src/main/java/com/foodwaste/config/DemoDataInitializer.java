package com.foodwaste.config;

import com.foodwaste.entity.User;
import com.foodwaste.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Admin User
        seedDemoUser(
                "System Administrator",
                "admin@fooddonation.com",
                "admin123",
                User.UserRole.ADMIN,
                "9999999999"
        );

        // Seed Demo Donor User
        seedDemoUser(
                "John Doe",
                "user@example.com",
                "password123",
                User.UserRole.DONOR,
                "1234567890"
        );

        // Seed Demo NGO User
        seedDemoUser(
                "Help Foundation",
                "ngo@example.com",
                "password123",
                User.UserRole.NGO,
                "9876543210"
        );

        // Seed Demo Analyst User
        seedDemoUser(
                "Data Analyst",
                "analyst@example.com",
                "password123",
                User.UserRole.ANALYST,
                "5555555555"
        );
    }

    private void seedDemoUser(String name, String email, String rawPassword, User.UserRole role, String phone) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setPhone(phone);
        user.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(user);
        log.info("Seeded demo login user: {}", email);
    }
}