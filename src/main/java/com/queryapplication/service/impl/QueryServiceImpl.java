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

        // Handle roles
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

                    // Set username and role for the user who added the answer
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
    public void addQueries(List<NewQueryDTO> newQueries) {
        newQueries.forEach(queryDTO -> {
            // Find the user who added the query
            Users user = userRepository.findById(queryDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Extract tag names from the request
            Set<String> tagNames = queryDTO.getTags().stream()
                    .map(TagDTO::getTagName)
                    .collect(Collectors.toSet());

            // Fetch existing tags from the database using the 'findByTagNameIn' method
            List<Tag> existingTagsList = tagRepository.findByTagNameIn(tagNames);
            Set<Tag> existingTagsSet = new HashSet<>(existingTagsList);

            // Determine which tags need to be created
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

                        // Fetch the TagGroup entity from the database
                        TagGroup tagGroup = tagGroupRepository.findByName(tagDTO.getTagGroupName())
                                .orElseThrow(() -> new IllegalArgumentException("Tag group not found for " + tagDTO.getTagGroupName()));

                        // Create a new Tag
                        Tag newTag = new Tag();
                        newTag.setTagName(tagName);
                        newTag.setTagGroup(tagGroup); // Set the TagGroup from the database
                        return newTag;
                    })
                    .collect(Collectors.toSet());

            // Save new tags to the database
            newTags = new HashSet<>(tagRepository.saveAll(newTags));

            // Combine existing and new tags
            existingTagsSet.addAll(newTags);

            // Create and save the query
            Query query = new Query();
            query.setQuestion(queryDTO.getQuestion());
            query.setAddedBy(user);
            query.setTags(existingTagsSet);
            queryRepository.save(query);
        });
    }


    @Override
    public void addAnswers(List<NewAnswerDTO> newAnswers) {
        newAnswers.forEach(answerDTO -> {
            Users user = userRepository.findById(answerDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Query query = queryRepository.findById(answerDTO.getQueryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

            Answer answer = new Answer();
            answer.setAnswer(answerDTO.getAnswer());
            answer.setAddedBy(user);
            answer.setQuery(query);

            answerRepository.save(answer);
        });
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
        // Delete all answers associated with the query before deleting the query itself
        answerRepository.deleteAll(query.getAnswers());
        queryRepository.delete(query);
    }

    @Override
    public void editQuery(Long queryId, NewQueryDTO newQueryDTO) {
        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with id " + queryId));

        // Find the user who is updating the query
        Users user = userRepository.findById(newQueryDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update question text
        query.setQuestion(newQueryDTO.getQuestion());
        query.setAddedBy(user);

        // Handle tags (update or add new tags)
        Set<String> tagNames = newQueryDTO.getTags().stream()
                .map(TagDTO::getTagName)
                .collect(Collectors.toSet());

        // Fetch existing tags from the database using 'findByTagNameIn' method
        List<Tag> existingTagsList = tagRepository.findByTagNameIn(tagNames);
        Set<Tag> existingTagsSet = new HashSet<>(existingTagsList);

        // Determine which tags need to be created
        Set<Tag> newTags = tagNames.stream()
                .filter(tagName -> !existingTagsSet.stream().anyMatch(tag -> tag.getTagName().equals(tagName)))
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
                })
                .collect(Collectors.toSet());

        // Save new tags
        tagRepository.saveAll(newTags);

        // Combine existing and new tags
        existingTagsSet.addAll(newTags);
        query.setTags(existingTagsSet);

        // Save updated query
        queryRepository.save(query);
    }


    @Override
    public void editAnswer(Long answerId, NewAnswerDTO newAnswerDTO) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));

        // Find the user who is updating the answer
        Users user = userRepository.findById(newAnswerDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update answer text
        answer.setAnswer(newAnswerDTO.getAnswer());
        answer.setAddedBy(user);

        // Save updated answer
        answerRepository.save(answer);
    }



    @Override
    public void copyAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id " + answerId));

        // Create a copy of the answer and save it
        Answer copy = new Answer();
        copy.setAnswer(answer.getAnswer());
        copy.setAddedBy(answer.getAddedBy()); // Keep the original user
        copy.setQuery(answer.getQuery()); // Keep the original query
        answerRepository.save(copy);
    }
}
