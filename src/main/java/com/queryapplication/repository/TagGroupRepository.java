package com.queryapplication.repository;

import com.queryapplication.entity.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagGroupRepository extends JpaRepository<TagGroup, Long> {
    Optional<TagGroup> findByName(String name);
}

