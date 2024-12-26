package com.queryapplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_groups", nullable = false)
    private TagGroup tagGroup;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + id +
                ", tagName='" + tagName + '\'' +
                ", tagGroup=" + tagGroup +
                ", createdAt=" + createdAt +
                '}';
    }
}
