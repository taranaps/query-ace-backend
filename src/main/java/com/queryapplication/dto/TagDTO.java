package com.queryapplication.dto;

import com.queryapplication.entity.TagGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {
    private String tagName;
    private TagGroup tagGroup; // E.g., "Company", "Category", etc.

}
