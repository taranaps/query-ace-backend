package com.queryapplication.repository;

import com.queryapplication.entity.PasswordChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordChangeRequestRepository extends JpaRepository<PasswordChangeRequest, Long> {
    Optional<PasswordChangeRequest> findByHashId(String hashId);
}
