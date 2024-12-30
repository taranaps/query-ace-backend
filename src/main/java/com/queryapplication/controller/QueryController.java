package com.queryapplication.controller;

import com.queryapplication.dto.*;
import com.queryapplication.entity.TagGroup;
import com.queryapplication.service.QueryService;
import com.queryapplication.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/queryapplication/queries")
public class QueryController {

    private final QueryService queryService;
    private final TagService tagService;

    @Autowired
    public QueryController(QueryService queryService, TagService tagService) {
        this.queryService = queryService;
        this.tagService = tagService;
    }

    @GetMapping
    public List<QueryDTO> getAllQueries() {
        return queryService.getAllQueries();
    }

    @GetMapping("/{id}")
    public QueryDTO getQueryById(@PathVariable Long id) {
        return queryService.getQueryById(id);
    }

    @GetMapping("/with-answers")
    public List<QueryWithAnswersDTO> getAllQueriesWithAnswers() {
        return queryService.getAllQueriesWithAnswers();
    }

    @GetMapping("/{id}/with-answers")
    public QueryWithAnswersDTO getQueryWithAnswersById(@PathVariable Long id) {
        return queryService.getQueryWithAnswersById(id);
    }

    @PostMapping
    public void addQueries(@RequestBody List<NewQueryDTO> newQueries) {
        queryService.addQueries(newQueries);
    }

    @PostMapping("/{id}/answers")
    public void addAnswers(@RequestBody List<NewAnswerDTO> newAnswers) {
        queryService.addAnswers(newAnswers);
    }

    // Delete a particular answer
    @DeleteMapping("/answers/{answerId}")
    public void deleteAnswer(@PathVariable Long answerId) {
        queryService.deleteAnswer(answerId);
    }

    @DeleteMapping("/{queryId}/answers")
    public ResponseEntity<Void> deleteAnswersForQuery(@PathVariable Long queryId) {
        queryService.deleteAllAnswersForQuery(queryId); // Service method to delete all answers for the given query
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{queryId}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long queryId) {
        queryService.deleteQuery(queryId); // Service method to delete the query and its answers
        return ResponseEntity.noContent().build();
    }

    // Edit a particular query (question)
    @PatchMapping("/{queryId}")
    public void editQuery(@PathVariable Long queryId, @RequestBody List<NewQueryDTO> newQueryDetails) {
        if (newQueryDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of queries.");
        }
        NewQueryDTO newQueryDTO = newQueryDetails.get(0); // Assuming only one query is passed in the body
        queryService.editQuery(queryId, newQueryDTO);
    }

    // Edit a particular answer
    @PatchMapping("/answers/{answerId}")
    public void editAnswer(@PathVariable Long answerId, @RequestBody List<NewAnswerDTO> newAnswerDetails) {
        if (newAnswerDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of answers.");
        }
        NewAnswerDTO newAnswerDTO = newAnswerDetails.get(0); // Assuming only one answer is passed in the body
        queryService.editAnswer(answerId, newAnswerDTO);
    }



    // Copy a particular answer
    @PostMapping("/answers/{answerId}/copy")
    public void copyAnswer(@PathVariable Long answerId) {
        queryService.copyAnswer(answerId);
    }

    // -------------------- Tag-related APIs --------------------

    // Get all tag groups
    @GetMapping("/tags/groups")
    public ResponseEntity<List<String>> getAllTagGroups() {
        return ResponseEntity.ok(tagService.getAllTagGroups());
    }

    // Get tags by group
    @GetMapping("/tags/group/{tagGroup}")
    public ResponseEntity<List<TagDTO>> getTagsByGroup(@PathVariable String tagGroup) {
        return ResponseEntity.ok(tagService.getTagsByGroup(tagGroup));
    }

    // Add a new tag
    @PostMapping("/tags")
    public ResponseEntity<TagDTO> addTag(@RequestBody TagDTO tagDTO) {
        return ResponseEntity.ok(tagService.addTag(tagDTO));
    }

    // Search for tags by name
    @GetMapping("/tags/search")
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam String tagName) {
        return ResponseEntity.ok(tagService.searchTags(tagName));
    }

    @GetMapping("/tags/details")
    public ResponseEntity<List<TagGroupDTO>> getTagGroups() {
        List<TagGroupDTO> tagGroups = tagService.getTagGroups();
        return ResponseEntity.ok(tagGroups);
    }

}
