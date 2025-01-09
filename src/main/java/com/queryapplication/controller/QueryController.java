package com.queryapplication.controller;

import com.queryapplication.dto.*;
import com.queryapplication.entity.Query;
import com.queryapplication.entity.TagGroup;
import com.queryapplication.exception.ResourceNotFoundException;
import com.queryapplication.service.QueryService;
import com.queryapplication.service.TagService;
import com.queryapplication.util.CategoryCompanyExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/queryapplication/queries")
public class QueryController {

    private final QueryService queryService;
    private final TagService tagService;
    private final CategoryCompanyExcelUtil categoryCompanyExcelUtil;


    @Autowired
    public QueryController(QueryService queryService, TagService tagService, CategoryCompanyExcelUtil categoryCompanyExcelUtil) {
        this.queryService = queryService;
        this.tagService = tagService;
        this.categoryCompanyExcelUtil = categoryCompanyExcelUtil;
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
    public ResponseEntity<List<Long>> addQueries(@RequestBody List<NewQueryDTO> newQueries) {
        List<Long> queryIds = queryService.addQueries(newQueries);
        return ResponseEntity.ok(queryIds);
    }


    @PostMapping("/id/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswers(@RequestBody List<NewAnswerDTO> newAnswers) {
        List<AnswerResponseDTO> response = queryService.addAnswers(newAnswers);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queryId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswersToQuery(
            @PathVariable Long queryId,
            @RequestBody List<AnswerRequestDTO> newAnswers) {
        List<AnswerResponseDTO> response = queryService.addAnswersToQuery(queryId, newAnswers);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Long>> addBulkQueries(@RequestBody List<BulkQueryDTO> bulkQueries) {
        List<Long> queryIds = queryService.addBulkQueries(bulkQueries);
        return ResponseEntity.ok(queryIds);
    }

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

    @PatchMapping("/{queryId}")
    public void editQuery(@PathVariable Long queryId, @RequestBody List<NewQueryDTO> newQueryDetails) {
        if (newQueryDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of queries.");
        }
        NewQueryDTO newQueryDTO = newQueryDetails.get(0); // Assuming only one query is passed in the body
        queryService.editQuery(queryId, newQueryDTO);
    }

    @PatchMapping("/answers/{answerId}")
    public void editAnswer(@PathVariable Long answerId, @RequestBody List<NewAnswerDTO> newAnswerDetails) {
        if (newAnswerDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of answers.");
        }
        NewAnswerDTO newAnswerDTO = newAnswerDetails.get(0); // Assuming only one answer is passed in the body
        queryService.editAnswer(answerId, newAnswerDTO);
    }


    @PostMapping("/answers/{answerId}/copy")
    public ResponseEntity<String> copyAnswer(@PathVariable Long answerId) {
        queryService.copyAnswer(answerId);
        return ResponseEntity.ok("Answer copied successfully. Copy count has been updated.");
    }

    // -------------------- Tag-related APIs --------------------

    @GetMapping("/tags/groups")
    public ResponseEntity<List<String>> getAllTagGroups() {
        return ResponseEntity.ok(tagService.getAllTagGroups());
    }

    @GetMapping("/tags/group/{tagGroup}")
    public ResponseEntity<List<TagDTO>> getTagsByGroup(@PathVariable String tagGroup) {
        return ResponseEntity.ok(tagService.getTagsByGroup(tagGroup));
    }

    @PostMapping("/tags/group")
    public ResponseEntity<String> createTagGroup(@RequestParam String groupName) {
        tagService.createTagGroup(groupName);
        return ResponseEntity.ok("Tag group created successfully.");
    }

    @PostMapping("/{queryId}/tags/add")
    public ResponseEntity<String> addTagToQuery(
            @PathVariable Long queryId,
            @RequestBody TagDTO tagDTO
    ) {
        tagService.addTagToQuery(queryId, tagDTO);
        return ResponseEntity.ok("Tag added to query successfully.");
    }

    @DeleteMapping("/{queryId}/tags/{tagId}/remove")
    public ResponseEntity<String> deleteTagFromQuery(
            @PathVariable Long queryId,
            @PathVariable Long tagId
    ) {
        tagService.deleteTagFromQuery(queryId, tagId);
        return ResponseEntity.ok("Tag removed from query successfully.");
    }


    @DeleteMapping("/tags/{id}")
    public ResponseEntity<String> deleteTagById(@PathVariable Long id) {
        tagService.deleteTagById(id);
        return ResponseEntity.ok("Tag deleted successfully.");
    }

    @DeleteMapping("/tags/group/{groupName}")
    public ResponseEntity<String> deleteTagGroup(@PathVariable String groupName) {
        tagService.deleteTagGroup(groupName);
        return ResponseEntity.ok("Tag group deleted successfully.");
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDTO> addTag(@RequestBody TagDTO tagDTO) {
        return ResponseEntity.ok(tagService.addTag(tagDTO));
    }

    @GetMapping("/tags/search")
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam String tagName) {
        return ResponseEntity.ok(tagService.searchTags(tagName));
    }

    @GetMapping("/tags/details")
    public ResponseEntity<List<TagGroupDTO>> getTagGroups() {
        List<TagGroupDTO> tagGroups = tagService.getTagGroups();
        return ResponseEntity.ok(tagGroups);
    }

    @GetMapping("/search")
    public ResponseEntity<List<QueryWithAnswersDTO>> searchQueries(
            @RequestParam(required = false) String questionText,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String tagGroup,
            @RequestParam(required = false) String answer) {

        List<QueryWithAnswersDTO> result = queryService.searchQueries(questionText, tags, tagGroup, answer);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    public ResponseEntity<List<QueryWithAnswersDTO>> searchQueriesByKeyword(@RequestBody SearchRequestDTO searchRequest) {
        List<QueryWithAnswersDTO> results = queryService.searchQueriesByKeyword(searchRequest.getKeyword());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }


            queryService.processFile(file, userId);

            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process the file: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            queryService.processExcel(file, userId);
            return ResponseEntity.ok("File uploaded and processed successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
        }

    @GetMapping("/companies")
    public ResponseEntity<List<String>> getCompanies() {

        List<String> companies = List.of("Company A", "Company B", "Company C", "Company D");
        return ResponseEntity.ok(companies);
    }
    @GetMapping("/filter")
    public ResponseEntity<List<QueryDTO>> filterQueriesByAddedByUsernames(
            @RequestParam List<String> addedByUsernames) {
        List<QueryDTO> filteredQueries = queryService.filterQueriesByAddedByUsernames(addedByUsernames);
        return ResponseEntity.ok(filteredQueries);
    }

}
