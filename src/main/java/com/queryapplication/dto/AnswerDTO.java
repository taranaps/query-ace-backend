package com.queryapplication.dto;

import com.queryapplication.entity.RoleName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnswerDTO {
    private Long id;
    private String answer;
    private Long usersId;
    private String usersUsername;
    private String email;
    private String firstName;
    private RoleName roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
