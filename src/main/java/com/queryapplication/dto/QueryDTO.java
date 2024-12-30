package com.queryapplication.dto;

import com.queryapplication.entity.RoleName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class QueryDTO {
    private Long id;
    private String question;
    private Long usersId;
    private String usersUsername;
    private String email;
    private String firstName;
    private RoleName roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<TagDTO> tags;
}
