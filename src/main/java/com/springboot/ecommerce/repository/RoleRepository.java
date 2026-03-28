package com.springboot.ecommerce.repository;

import com.springboot.ecommerce.entity.Role;
import com.springboot.ecommerce.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);
}
