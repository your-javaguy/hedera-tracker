package com.hedera.tracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hedera.tracker.model.Role;
import com.hedera.tracker.model.Role.ERole;

/**
 * Repository for managing Role entities
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Find a role by its name
     * 
     * @param name the role name to search for
     * @return an Optional containing the role if found
     */
    Optional<Role> findByName(ERole name);
} 