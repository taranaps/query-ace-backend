package com.queryapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagGroupDTO {
    private String tagGroupName;
    private List<String> tagNames;
}
