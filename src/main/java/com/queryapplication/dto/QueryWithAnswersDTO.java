package com.queryapplication.dto;

import com.queryapplication.entity.RoleName;
import lombok.Data;

import java.util.Set;

@Data
public class QueryWithAnswersDTO {
    private String question;
    private String usersUsername;
    private RoleName roleRoleName;
    private Set<TagDTO> tags;
    private Set<AnswerDTO> answers;
}
