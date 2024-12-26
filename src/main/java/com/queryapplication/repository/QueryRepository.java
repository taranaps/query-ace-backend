package com.queryapplication.repository;

import com.queryapplication.entity.Query;
import com.queryapplication.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryRepository extends JpaRepository<Query, Long> {
    List<Query> findByQuestionContaining(String keyword); // For keyword search
    public List<Query> findByAddedBy(Users addedBy);
}
