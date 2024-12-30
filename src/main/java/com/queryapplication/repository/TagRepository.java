package com.queryapplication.repository;

import com.queryapplication.entity.Tag;
import com.queryapplication.entity.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByTagGroup(TagGroup tagGroup); // Fetch tags by group (e.g., Company, Category)
    List<Tag> findByTagNameIn(Set<String> tagNames);
    Optional<Tag> findByTagName(String tagName);
    List<Tag> findByTagNameContainingIgnoreCase(String tagName);
}
