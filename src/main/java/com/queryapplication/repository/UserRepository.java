package com.queryapplication.repository;

import com.queryapplication.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    // Optionally, find by username or email if needed
    Optional<Users> findByUsernameOrEmail(String username, String email);
}
