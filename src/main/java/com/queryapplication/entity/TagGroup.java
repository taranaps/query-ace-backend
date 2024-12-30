package com.queryapplication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag_group")
@Getter
@Setter
@NoArgsConstructor
public class TagGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "tagGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tag> tags = new HashSet<>();
}

