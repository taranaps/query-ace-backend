package com.queryapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkQueryDTO {
    private String question;
    private Long userId;
    private Set<TagDTO> tags;
    private List<AnswerRequestDTO> answers;
}
