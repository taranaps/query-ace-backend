package com.queryapplication.dto;

import com.queryapplication.entity.RoleName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class QueryDTO {
    private String question;
    private String usersUsername;
    private RoleName roleName;
    private Set<TagDTO> tags; // Associated tags
}
