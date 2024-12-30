package com.queryapplication.service;

import com.queryapplication.dto.TagDTO;
import com.queryapplication.dto.TagGroupDTO;

import java.util.List;

public interface TagService {
    List<String> getAllTagGroups();

    List<TagDTO> getTagsByGroup(String groupName);

    TagDTO addTag(TagDTO tagDTO);

    List<TagDTO> searchTags(String tagName);

    void createTagGroup(String groupName);

    List<TagGroupDTO> getTagGroups();
}
