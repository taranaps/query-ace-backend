package com.queryapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewQueryDTO {
    private String question;
    private Long userId;
    private Set<TagDTO> tags;
}
