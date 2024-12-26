package com.queryapplication.repository;

import com.queryapplication.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByTagGroup(String tagGroup); // Fetch tags by group (e.g., Company, Category)
    List<Tag> findByTagNameIn(Set<String> tagNames);
}
