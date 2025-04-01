package com.hedera.tracker.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hedera.tracker.model.Role;
import com.hedera.tracker.model.User;
import com.hedera.tracker.model.Role.ERole;
import com.hedera.tracker.repository.RoleRepository;
import com.hedera.tracker.repository.UserRepository;

/**
 * Initializes a default admin user
 */
@Component
@Order(2) // Run after DatabaseInitializer
public class DefaultAdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking for admin user...");
        
        // Check if admin user exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            System.out.println("Admin user does not exist, creating...");
            
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin"));

            // Assign admin role
            Set<Role> roles = new HashSet<>();
            try {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Admin Role not found."));
                roles.add(adminRole);
                
                // Also add USER role
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: User Role not found."));
                roles.add(userRole);
                
                admin.setRoles(roles);
                userRepository.save(admin);
                System.out.println("Created default admin user with roles: " + roles);
            } catch (Exception e) {
                System.err.println("Error creating admin user: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("Admin user already exists");
        }
    }
} 