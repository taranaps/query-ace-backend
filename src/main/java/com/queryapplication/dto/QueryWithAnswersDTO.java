package com.queryapplication.dto;

import com.queryapplication.entity.RoleName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class QueryWithAnswersDTO {
    private Long id;
    private String question;
    private Long usersId;
    private String usersUsername;
    private String email;
    private String firstName;
    private RoleName roleRoleName;
    private Set<TagDTO> tags;
    private LocalDateTime queryCreatedAt;
    private LocalDateTime queryUpdatedAt;
    private Set<AnswerDTO> answers;
}
