package com.laforesta.api.user.repository;

import com.laforesta.api.user.entity.Role;
import com.laforesta.api.user.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);
}