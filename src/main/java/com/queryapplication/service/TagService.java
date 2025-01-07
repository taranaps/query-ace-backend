package com.queryapplication.service;

import com.queryapplication.dto.TagDTO;
import com.queryapplication.dto.TagGroupDTO;

import java.util.List;

public interface TagService {
    List<String> getAllTagGroups();

    List<TagDTO> getTagsByGroup(String groupName);

    void addTagToQuery(Long queryId, TagDTO tagDTO);

    void deleteTagFromQuery(Long queryId, Long tagId);

    void createTagGroup(String groupName);

    void deleteTagById(Long tagId);

    void deleteTagGroup(String groupName);

    TagDTO addTag(TagDTO tagDTO);

    List<TagDTO> searchTags(String tagName);

    List<TagGroupDTO> getTagGroups();
}
