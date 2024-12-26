package com.queryapplication.repository;

import com.queryapplication.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName); // Fetch role by firstName (e.g., Admin, Super Admin)
}
