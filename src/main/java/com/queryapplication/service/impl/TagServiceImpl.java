package com.queryapplication.service.impl;

import com.queryapplication.dto.TagDTO;
import com.queryapplication.dto.TagGroupDTO;
import com.queryapplication.entity.Query;
import com.queryapplication.entity.Tag;
import com.queryapplication.entity.TagGroup;
import com.queryapplication.repository.QueryRepository;
import com.queryapplication.repository.TagGroupRepository;
import com.queryapplication.repository.TagRepository;
import com.queryapplication.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagGroupRepository tagGroupRepository;
    private final QueryRepository queryRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, TagGroupRepository tagGroupRepository, QueryRepository queryRepository) {
        this.tagRepository = tagRepository;
        this.tagGroupRepository = tagGroupRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    public List<String> getAllTagGroups() {
        return tagGroupRepository.findAll()
                .stream()
                .map(TagGroup::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void addTagToQuery(Long queryId, TagDTO tagDTO) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found with ID: " + queryId));

        TagGroup tagGroup = tagGroupRepository.findByName(tagDTO.getTagGroupName())
                .orElseThrow(() -> new IllegalArgumentException("Tag group not found: " + tagDTO.getTagGroupName()));

        Tag tag = tagRepository.findByTagName(tagDTO.getTagName())
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setTagName(tagDTO.getTagName());
                    newTag.setTagGroup(tagGroup);
                    newTag.setCreatedAt(LocalDateTime.now());
                    return tagRepository.save(newTag);
                });

        query.getTags().add(tag);
        queryRepository.save(query);
    }

    @Override
    public void deleteTagFromQuery(Long queryId, Long tagId) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new IllegalArgumentException("Query not found with ID: " + queryId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagId));

        if (query.getTags().contains(tag)) {
            query.getTags().remove(tag);
            queryRepository.save(query);
        } else {
            throw new IllegalArgumentException("Tag is not associated with the query.");
        }

        boolean tagIsUsedElsewhere = queryRepository.findByTag(tag).size() > 0;
        if (!tagIsUsedElsewhere) {
            tagRepository.delete(tag);
        }
    }



    @Override
    public void deleteTagById(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with ID: " + tagId));
        tagRepository.delete(tag);
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
    public void deleteTagGroup(String groupName) {
        TagGroup tagGroup = tagGroupRepository.findByName(groupName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tag group: " + groupName));
        tagGroupRepository.delete(tagGroup);
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
                .orElseGet(() -> {
                    TagGroup newTagGroup = new TagGroup();
                    newTagGroup.setName(tagDTO.getTagGroupName());
                    return tagGroupRepository.save(newTagGroup);
                });
        Tag tag = tagRepository.findByTagName(tagDTO.getTagName())
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setTagName(tagDTO.getTagName());
                    newTag.setTagGroup(tagGroup);
                    newTag.setCreatedAt(LocalDateTime.now());
                    return tagRepository.save(newTag);
                });
        if (!tag.getTagGroup().getName().equals(tagDTO.getTagGroupName())) {
            tag.setTagGroup(tagGroup);
            tag = tagRepository.save(tag);
        }
        return new TagDTO(tag.getTagName(), tag.getTagGroup().getName());
    }

    @Override
    public List<TagDTO> searchTags(String tagName) {
        return tagRepository.findByTagNameContainingIgnoreCase(tagName)
                .stream()
                .map(tag -> new TagDTO(tag.getTagName(), tag.getTagGroup().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TagGroupDTO> getTagGroups() {
        return tagGroupRepository.findAll()
                .stream()
                .map(tagGroup -> {
                    TagGroupDTO dto = new TagGroupDTO();
                    dto.setTagGroupName(tagGroup.getName());
                    dto.setTagNames(tagGroup.getTags()
                            .stream()
                            .map(Tag::getTagName) // No more error here
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
