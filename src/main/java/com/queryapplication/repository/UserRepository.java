package com.queryapplication.repository;

import com.queryapplication.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username); // Find user by username
    List<Users> findByStatus(String status); // Active or Inactive users
}
