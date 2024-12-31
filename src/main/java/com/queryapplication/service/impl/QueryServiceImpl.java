package com.queryapplication.service.impl;

import com.queryapplication.dto.*;
import com.queryapplication.entity.*;
import com.queryapplication.exception.ResourceNotFoundException;
import com.queryapplication.repository.*;
import com.queryapplication.service.QueryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QueryServiceImpl implements QueryService {

    private final QueryRepository queryRepository;
    private final AnswerRepository answerRepository;
    private final TagRepository tagRepository;
    private final TagGroupRepository tagGroupRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public QueryServiceImpl(QueryRepository queryRepository, AnswerRepository answerRepository, TagRepository tagRepository, TagGroupRepository tagGroupRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
        this.tagRepository = tagRepository;
        this.tagGroupRepository = tagGroupRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<QueryDTO> getAllQueries() {
        return queryRepository.findAll()
                .stream()
                .map(this::mapToQueryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryDTO getQueryById(Long id) {
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with id " + id));
        return mapToQueryDTO(query);
    }

    private QueryDTO mapToQueryDTO(Query query) {
        QueryDTO dto = modelMapper.map(query, QueryDTO.class);
        dto.setId(query.getId());
        dto.setUsersId(query.getAddedBy().getId());
        dto.setUsersUsername(query.getAddedBy().getUsername());
        dto.setFirstName(query.getAddedBy().getFirstName());
        dto.setEmail(query.getAddedBy().getEmail());
        dto.setCreatedAt(query.getCreatedAt());
        dto.setUpdatedAt(query.getUpdatedAt());

        Role role = query.getAddedBy().getRoles().stream().findFirst().orElse(null);
        dto.setRoleName(role != null ? role.getRoleName() : null);

        dto.setTags(query.getTags().stream()
                .map(tag -> new TagDTO(tag.getTagName(), tag.getTagGroup().getName()))
                .collect(Collectors.toSet()));

        return dto;
    }

    @Override
    public List<QueryWithAnswersDTO> getAllQueriesWithAnswers() {
        return queryRepository.findAll()
                .stream()
                .map(this::mapToQueryWithAnswersDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWithAnswersDTO getQueryWithAnswersById(Long id) {
        Query query = queryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with id " + id));
        return mapToQueryWithAnswersDTO(query);
    }

    private QueryWithAnswersDTO mapToQueryWithAnswersDTO(Query query) {
        QueryWithAnswersDTO dto = new QueryWithAnswersDTO();
        dto.setId(query.getId());
        dto.setQuestion(query.getQuestion());
        dto.setQueryCreatedAt(query.getCreatedAt());
        dto.setQueryUpdatedAt(query.getUpdatedAt());
        dto.setUsersId(query.getAddedBy().getId());
        dto.setUsersUsername(query.getAddedBy().getUsername());
        dto.setFirstName(query.getAddedBy().getFirstName());
        dto.setEmail(query.getAddedBy().getEmail());

        Role role = query.getAddedBy().getRoles().stream().findFirst().orElse(null);
        dto.setRoleRoleName(role != null ? role.getRoleName() : null);

        dto.setTags(query.getTags().stream()
                .map(tag -> new TagDTO(tag.getTagName(), tag.getTagGroup().getName()))
                .collect(Collectors.toSet()));

        dto.setAnswers(query.getAnswers().stream()
                .map(answer -> {
                    AnswerDTO answerDTO = new AnswerDTO();
                    answerDTO.setId(answer.getId());
                    answerDTO.setUsersId(answer.getAddedBy().getId());
                    answerDTO.setUsersUsername(answer.getAddedBy().getUsername());
                    answerDTO.setFirstName(answer.getAddedBy().getFirstName());
                    answerDTO.setEmail(answer.getAddedBy().getEmail());
                    answerDTO.setAnswer(answer.getAnswer());
                    answerDTO.setCreatedAt(answer.getCreatedAt());
                    answerDTO.setUpdatedAt(answer.getUpdatedAt());

                    Users addedBy = answer.getAddedBy();
                    if (addedBy != null) {
                        answerDTO.setUsersUsername(addedBy.getUsername());
                        Role answerRole = addedBy.getRoles().stream().findFirst().orElse(null);
                        answerDTO.setRoleName(answerRole != null ? answerRole.getRoleName() : null);
                    }

                    return answerDTO;
                })
                .collect(Collectors.toSet()));

        return dto;
    }

    @Override
    public List<Long> addQueries(List<NewQueryDTO> newQueries) {
        return newQueries.stream().map(queryDTO -> {

            Users user = userRepository.findById(queryDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Set<String> tagNames = queryDTO.getTags().stream()
                    .map(TagDTO::getTagName)
                    .collect(Collectors.toSet());

            List<Tag> existingTagsList = tagRepository.findByTagNameIn(tagNames);
            Set<Tag> existingTagsSet = new HashSet<>(existingTagsList);

            Set<String> existingTagNames = existingTagsSet.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toSet());
            Set<Tag> newTags = tagNames.stream()
                    .filter(tagName -> !existingTagNames.contains(tagName))
                    .map(tagName -> {
                        // Get the tag group from the DTO
                        TagDTO tagDTO = queryDTO.getTags().stream()
                                .filter(tag -> tag.getTagName().equals(tagName))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("Tag group missing for " + tagName));

                        TagGroup tagGroup = tagGroupRepository.findByName(tagDTO.getTagGroupName())
                                .orElseThrow(() -> new IllegalArgumentException("Tag group not found for " + tagDTO.getTagGroupName()));

                        Tag newTag = new Tag();
                        newTag.setTagName(tagName);
                        newTag.setTagGroup(tagGroup);
                        return newTag;
                    })
                    .collect(Collectors.toSet());

            newTags = new HashSet<>(tagRepository.saveAll(newTags));

            existingTagsSet.addAll(newTags);

            Query query = new Query();
            query.setQuestion(queryDTO.getQuestion());
            query.setAddedBy(user);
            query.setTags(existingTagsSet);
            Query savedQuery = queryRepository.save(query);

            return savedQuery.getId();
        }).collect(Collectors.toList());
    }

    @Override
    public List<AnswerResponseDTO> addAnswers(List<NewAnswerDTO> newAnswers) {
        return newAnswers.stream().map(answerDTO -> {
            Users user = userRepository.findById(answerDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Query query = queryRepository.findById(answerDTO.getQueryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

            Answer answer = new Answer();
            answer.setAnswer(answerDTO.getAnswer());
            answer.setAddedBy(user);
            answer.setQuery(query);

            Answer savedAnswer = answerRepository.save(answer);

            return new AnswerResponseDTO(savedAnswer.getId(), savedAnswer.getQuery().getId());
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));
        answerRepository.delete(answer);
    }

    @Override
    public void deleteAllAnswersForQuery(Long queryId) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with id " + queryId));
        answerRepository.deleteAll(query.getAnswers());
    }

    @Override
    public void deleteQuery(Long queryId) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with id " + queryId));

        answerRepository.deleteAll(query.getAnswers());
        queryRepository.delete(query);
    }

    @Override
    public void editQuery(Long queryId, NewQueryDTO newQueryDTO) {
        Query existingQuery = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with id " + queryId));

        existingQuery.setQuestion(newQueryDTO.getQuestion());

        Set<String> tagNames = newQueryDTO.getTags().stream()
                .map(TagDTO::getTagName)
                .collect(Collectors.toSet());
        Set<Tag> existingTags = new HashSet<>(tagRepository.findByTagNameIn(tagNames));

        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());

        Set<Tag> newTags = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .map(tagName -> {
                    TagDTO tagDTO = newQueryDTO.getTags().stream()
                            .filter(tag -> tag.getTagName().equals(tagName))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Tag group missing for " + tagName));
                    TagGroup tagGroup = tagGroupRepository.findByName(tagDTO.getTagGroupName())
                            .orElseThrow(() -> new IllegalArgumentException("Tag group not found for " + tagDTO.getTagGroupName()));

                    Tag newTag = new Tag();
                    newTag.setTagName(tagName);
                    newTag.setTagGroup(tagGroup);
                    return newTag;
                }).collect(Collectors.toSet());

        newTags = new HashSet<>(tagRepository.saveAll(newTags));
        existingTags.addAll(newTags);

        existingQuery.setTags(existingTags);

        queryRepository.save(existingQuery);
    }

    @Override
    public void editAnswer(Long answerId, NewAnswerDTO newAnswerDTO) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));

        Users user = userRepository.findById(newAnswerDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        answer.setAnswer(newAnswerDTO.getAnswer());
        answer.setAddedBy(user);

        answerRepository.save(answer);
    }

    @Override
    public void copyAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));

        Answer copy = new Answer();
        copy.setAnswer(answer.getAnswer());
        copy.setAddedBy(answer.getAddedBy());
        copy.setQuery(answer.getQuery());
        answerRepository.save(copy);
    }

    @Override
    public List<QueryWithAnswersDTO> searchQueries(String questionText, List<String> tags, String tagGroup, String answer) {
        // Using the repository method to perform the search
        List<Query> queries = queryRepository.searchQueries(questionText, tags, tagGroup, answer);

        return queries.stream()
                .map(this::mapToQueryWithAnswersDTO)
                .collect(Collectors.toList());
    }

}
