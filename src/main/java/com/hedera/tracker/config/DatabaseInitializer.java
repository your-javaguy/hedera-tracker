package com.hedera.tracker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hedera.tracker.model.Role;
import com.hedera.tracker.model.Role.ERole;
import com.hedera.tracker.repository.RoleRepository;

/**
 * Database initializer to set up default roles
 */
@Component
@Order(1) // Run this before other initializers
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing default roles...");
        
        // Initialize default roles if they don't exist
        for (ERole role : ERole.values()) {
            if (roleRepository.findByName(role).isEmpty()) {
                Role newRole = new Role();
                newRole.setName(role);
                roleRepository.save(newRole);
                System.out.println("Added role: " + role);
            } else {
                System.out.println("Role already exists: " + role);
            }
        }
    }
} 