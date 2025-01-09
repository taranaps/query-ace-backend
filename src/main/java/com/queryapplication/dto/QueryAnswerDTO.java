package com.queryapplication.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueryAnswerDTO {
    private String question;
    private int highestCopyCount;
    private LocalDateTime createdAt;
}
