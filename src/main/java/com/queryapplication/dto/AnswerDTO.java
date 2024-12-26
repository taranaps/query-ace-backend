package com.queryapplication.dto;

import com.queryapplication.entity.RoleName;
import lombok.Data;

@Data
public class AnswerDTO {
    private String answer;
    private String usersUsername;
    private RoleName roleName;
}
