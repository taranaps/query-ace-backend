package com.queryapplication.repository;

import com.queryapplication.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QueryRepository extends JpaRepository<com.queryapplication.entity.Query, Long> {
    List<com.queryapplication.entity.Query> findByQuestionContaining(String keyword);
    List<com.queryapplication.entity.Query> findByAddedBy(Users addedBy);

    @Query("SELECT q FROM com.queryapplication.entity.Query q " +
            "JOIN q.tags t " +
            "JOIN t.tagGroup tg " +
            "LEFT JOIN q.answers a " +
            "WHERE (:questionText IS NULL OR LOWER(q.question) LIKE LOWER(CONCAT('%', :questionText, '%'))) " +
            "AND (:tags IS NULL OR t.tagName IN :tags) " +
            "AND (:tagGroup IS NULL OR tg.name = :tagGroup) " +
            "AND (:answer IS NULL OR a.answer LIKE LOWER(CONCAT('%', :answer, '%')))")
    List<com.queryapplication.entity.Query> searchQueries(
            @Param("questionText") String questionText,
            @Param("tags") List<String> tags,
            @Param("tagGroup") String tagGroup,
            @Param("answer") String answer);

    @Query("SELECT q FROM com.queryapplication.entity.Query q " +
            "JOIN q.tags t " +
            "JOIN t.tagGroup tg " +
            "LEFT JOIN q.answers a " +
            "WHERE LOWER(q.question) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.answer) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.tagName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(tg.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<com.queryapplication.entity.Query> searchQueriesByKeyword(String keyword);
}
