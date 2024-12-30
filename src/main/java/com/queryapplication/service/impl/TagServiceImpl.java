package com.queryapplication.service.impl;

import com.queryapplication.dto.TagDTO;
import com.queryapplication.entity.Tag;
import com.queryapplication.entity.TagGroup;
import com.queryapplication.repository.TagGroupRepository;
import com.queryapplication.repository.TagRepository;
import com.queryapplication.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagGroupRepository tagGroupRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, TagGroupRepository tagGroupRepository) {
        this.tagRepository = tagRepository;
        this.tagGroupRepository = tagGroupRepository;
    }

    @Override
    public List<String> getAllTagGroups() {
        return tagGroupRepository.findAll()
                .stream()
                .map(TagGroup::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<TagDTO> getTagsByGroup(String groupName) {
        TagGroup tagGroup = tagGroupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tag group: " + groupName));
        return tagRepository.findByTagGroup(tagGroup)
                .stream()
                .map(tag -> new TagDTO(tag.getTagName(), tagGroup.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void createTagGroup(String groupName) {
        if (tagGroupRepository.findByName(groupName).isPresent()) {
            throw new IllegalArgumentException("Tag group already exists: " + groupName);
        }

        TagGroup tagGroup = new TagGroup();
        tagGroup.setName(groupName);
        tagGroupRepository.save(tagGroup);
    }

    @Override
    public TagDTO addTag(TagDTO tagDTO) {
        TagGroup tagGroup = tagGroupRepository.findByName(tagDTO.getTagGroupName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid tag group: " + tagDTO.getTagGroupName()));

        if (tagRepository.findByTagName(tagDTO.getTagName()).isPresent()) {
            throw new IllegalArgumentException("Tag already exists: " + tagDTO.getTagName());
        }

        Tag tag = new Tag();
        tag.setTagName(tagDTO.getTagName());
        tag.setTagGroup(tagGroup);
        tag.setCreatedAt(LocalDateTime.now());

        Tag savedTag = tagRepository.save(tag);
        return new TagDTO(savedTag.getTagName(), savedTag.getTagGroup().getName());
    }

    @Override
    public List<TagDTO> searchTags(String tagName) {
        return tagRepository.findByTagNameContainingIgnoreCase(tagName)
                .stream()
                .map(tag -> new TagDTO(tag.getTagName(), tag.getTagGroup().getName()))
                .collect(Collectors.toList());
    }
}
