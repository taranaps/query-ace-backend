package com.queryapplication.repository;

import com.queryapplication.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQueryId(Long queryId);
}
