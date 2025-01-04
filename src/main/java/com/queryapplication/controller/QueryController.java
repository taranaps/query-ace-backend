package com.queryapplication.controller;

import com.queryapplication.dto.*;
import com.queryapplication.entity.TagGroup;
import com.queryapplication.entity.Users;
import com.queryapplication.service.ActivityLogService; // Import ActivityLogService
import com.queryapplication.service.QueryService;
import com.queryapplication.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ActivityLogService activityLogService; // Inject ActivityLogService

    @Autowired
    public QueryController(QueryService queryService, TagService tagService, ActivityLogService activityLogService) {
        this.queryService = queryService;
        this.tagService = tagService;
        this.activityLogService = activityLogService; // Assign the injected service
    }

    @GetMapping
    public List<QueryDTO> getAllQueries() {
        List<QueryDTO> queries = queryService.getAllQueries();
        // Log activity for viewing queries
        activityLogService.logActivity(new Users(), "viewed", "all queries");
        return queries;
    }

    @GetMapping("/{id}")
    public QueryDTO getQueryById(@PathVariable Long id) {
        QueryDTO query = queryService.getQueryById(id);
        // Log activity for viewing a specific query
        activityLogService.logActivity(new Users(), "viewed", "query with ID " + id);
        return query;
    }

    @PostMapping
    public ResponseEntity<List<Long>> addQueries(@RequestBody List<NewQueryDTO> newQueries) {
        List<Long> queryIds = queryService.addQueries(newQueries);
        // Log activity for adding queries
        activityLogService.logActivity(new Users(), "added", "queries with IDs " + queryIds);
        return ResponseEntity.ok(queryIds);
    }

    @PostMapping("/id/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswers(@RequestBody List<NewAnswerDTO> newAnswers) {
        List<AnswerResponseDTO> response = queryService.addAnswers(newAnswers);
        // Log activity for adding answers
        activityLogService.logActivity(new Users(), "added", "answers");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queryId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswersToQuery(
            @PathVariable Long queryId,
            @RequestBody List<AnswerRequestDTO> newAnswers) {
        List<AnswerResponseDTO> response = queryService.addAnswersToQuery(queryId, newAnswers);
        // Log activity for adding answers to a query
        activityLogService.logActivity(new Users(), "added", "answers to query ID " + queryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Long>> addBulkQueries(@RequestBody List<BulkQueryDTO> bulkQueries) {
        List<Long> queryIds = queryService.addBulkQueries(bulkQueries);
        // Log activity for adding bulk queries
        activityLogService.logActivity(new Users(), "added", "bulk queries");
        return ResponseEntity.ok(queryIds);
    }

    @DeleteMapping("/answers/{answerId}")
    public void deleteAnswer(@PathVariable Long answerId) {
        queryService.deleteAnswer(answerId);
        // Log activity for deleting an answer
        activityLogService.logActivity(new Users(), "deleted", "answer with ID " + answerId);
    }

    @DeleteMapping("/{queryId}/answers")
    public ResponseEntity<Void> deleteAnswersForQuery(@PathVariable Long queryId) {
        queryService.deleteAllAnswersForQuery(queryId); // Service method to delete all answers for the given query
        // Log activity for deleting all answers for a query
        activityLogService.logActivity(new Users(), "deleted", "all answers for query ID " + queryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{queryId}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long queryId) {
        queryService.deleteQuery(queryId); // Service method to delete the query and its answers
        // Log activity for deleting a query
        activityLogService.logActivity(new Users(), "deleted", "query with ID " + queryId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{queryId}")
    public void editQuery(@PathVariable Long queryId, @RequestBody List<NewQueryDTO> newQueryDetails) {
        if (newQueryDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of queries.");
        }
        NewQueryDTO newQueryDTO = newQueryDetails.get(0); // Assuming only one query is passed in the body
        queryService.editQuery(queryId, newQueryDTO);
        // Log activity for editing a query
        activityLogService.logActivity(new Users(), "edited", "query with ID " + queryId);
    }

    @PatchMapping("/answers/{answerId}")
    public void editAnswer(@PathVariable Long answerId, @RequestBody List<NewAnswerDTO> newAnswerDetails) {
        if (newAnswerDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of answers.");
        }
        NewAnswerDTO newAnswerDTO = newAnswerDetails.get(0); // Assuming only one answer is passed in the body
        queryService.editAnswer(answerId, newAnswerDTO);
        // Log activity for editing an answer
        activityLogService.logActivity(new Users(), "edited", "answer with ID " + answerId);
    }

    @PostMapping("/answers/{answerId}/copy")
    public void copyAnswer(@PathVariable Long answerId) {
        queryService.copyAnswer(answerId);
        // Log activity for copying an answer
        activityLogService.logActivity(new Users(), "copied", "answer with ID " + answerId);
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
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }

            queryService.processFile(file);

            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process the file: " + e.getMessage());
        }
    }

    @PostMapping("/upload-file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }
            queryService.processFileReader(file);
            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process the file: " + e.getMessage());
        }
    }
}
