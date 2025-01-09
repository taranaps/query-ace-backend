package com.queryapplication.controller;

import com.queryapplication.dto.*;
import com.queryapplication.entity.Query;
import com.queryapplication.entity.TagGroup;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.QueryService;
import com.queryapplication.service.TagService;
import com.queryapplication.service.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "Bearer Authentication")

public class QueryController {

    private final QueryService queryService;
    private final TagService tagService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    @Autowired
    public QueryController(QueryService queryService, TagService tagService, ActivityLogService activityLogService, UserRepository userRepository) {
        this.queryService = queryService;
        this.tagService = tagService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<QueryDTO> getAllQueries() {
        List<QueryDTO> queries = queryService.getAllQueries();
        if (!queries.isEmpty()) {
            Users user = userRepository.findById(queries.get(0).getUsersId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            activityLogService.logActivity( "viewed", "all queries");
        }
        return queries;
    }

    @GetMapping("/{id}")
    public QueryDTO getQueryById(@PathVariable Long id) {
        QueryDTO query = queryService.getQueryById(id);
        Users user = userRepository.findById(query.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity("viewed", "query ID: " + id);
        return query;
    }

    @GetMapping("/with-answers")
    public List<QueryWithAnswersDTO> getAllQueriesWithAnswers() {
        List<QueryWithAnswersDTO> queries = queryService.getAllQueriesWithAnswers();
        if (!queries.isEmpty()) {
            Users user = userRepository.findById(queries.get(0).getUsersId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            activityLogService.logActivity( "viewed", "all queries with answers");
        }
        return queries;
    }

    @GetMapping("/{id}/with-answers")
    public QueryWithAnswersDTO getQueryWithAnswersById(@PathVariable Long id) {
        QueryWithAnswersDTO query = queryService.getQueryWithAnswersById(id);
        Users user = userRepository.findById(query.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity("viewed", "query with answers ID: " + id);
        return query;
    }

    @PostMapping
    public ResponseEntity<List<Long>> addQueries(@RequestBody List<NewQueryDTO> newQueries) {
        List<Long> queryIds = queryService.addQueries(newQueries);
        Users user = userRepository.findById(newQueries.get(0).getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity( "added", "queries with IDs: " + queryIds);
        return ResponseEntity.ok(queryIds);
    }


    @PostMapping("/id/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswers(@RequestBody List<NewAnswerDTO> newAnswers) {
        List<AnswerResponseDTO> response = queryService.addAnswers(newAnswers);
        Users user = userRepository.findById(newAnswers.get(0).getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity("added", "new answers");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queryId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswersToQuery(
            @PathVariable Long queryId,
            @RequestBody List<AnswerRequestDTO> newAnswers) {
        List<AnswerResponseDTO> response = queryService.addAnswersToQuery(queryId, newAnswers);
        Users user = userRepository.findById(newAnswers.get(0).getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity("added", "answers to query ID: " + queryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Long>> addBulkQueries(@RequestBody List<BulkQueryDTO> bulkQueries) {
        List<Long> queryIds = queryService.addBulkQueries(bulkQueries);
        Users user = userRepository.findById(bulkQueries.get(0).getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity( "added", "bulk queries with IDs: " + queryIds);
        return ResponseEntity.ok(queryIds);
    }

    @DeleteMapping("/answers/{answerId}")
    public void deleteAnswer(@PathVariable Long answerId) {
        QueryDTO query = queryService.getQueryById(answerId);
        Users user = userRepository.findById(query.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        queryService.deleteAnswer(answerId);
        activityLogService.logActivity("deleted", "answer ID: " + answerId);
    }

    @DeleteMapping("/{queryId}/answers")
    public ResponseEntity<Void> deleteAnswersForQuery(@PathVariable Long queryId) {
        QueryDTO query = queryService.getQueryById(queryId);
        Users user = userRepository.findById(query.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        queryService.deleteAllAnswersForQuery(queryId);
        activityLogService.logActivity("deleted", "all answers of query ID: " + queryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{queryId}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long queryId) {
        QueryDTO query = queryService.getQueryById(queryId);
        Users user = userRepository.findById(query.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        queryService.deleteQuery(queryId);
        activityLogService.logActivity("deleted", "query ID: " + queryId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{queryId}")
    public void editQuery(@PathVariable Long queryId, @RequestBody List<NewQueryDTO> newQueryDetails) {
        if (newQueryDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of queries.");
        }
        NewQueryDTO newQueryDTO = newQueryDetails.get(0);
        QueryDTO oldQuery = queryService.getQueryById(queryId);
        Users user = userRepository.findById(newQueryDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        queryService.editQuery(queryId, newQueryDTO);
        activityLogService.logActivity( "edited", String.format("query ID: %d from '%s' to '%s'",
                queryId, oldQuery.getQuestion(), newQueryDTO.getQuestion()));
    }

    @PatchMapping("/answers/{answerId}")
    public void editAnswer(@PathVariable Long answerId, @RequestBody List<NewAnswerDTO> newAnswerDetails) {
        if (newAnswerDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of answers.");
        }
        NewAnswerDTO newAnswerDTO = newAnswerDetails.get(0);
        Users user = userRepository.findById(newAnswerDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        queryService.editAnswer(answerId, newAnswerDTO);
        activityLogService.logActivity( "edited", "answer ID: " + answerId);
    }



    @PostMapping("/answers/{answerId}/copy")
    public void copyAnswer(@PathVariable Long answerId) {
        QueryDTO query = queryService.getQueryById(answerId);
        Users user = userRepository.findById(query.getUsersId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        queryService.copyAnswer(answerId);
        activityLogService.logActivity( "copied", "answer ID: " + answerId);
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


    @GetMapping("/tags/search")
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam String tagName) {
        return ResponseEntity.ok(tagService.searchTags(tagName));
    }

    @GetMapping("/tags/details")
    public ResponseEntity<List<TagGroupDTO>> getTagGroups() {
        List<TagGroupDTO> tagGroups = tagService.getTagGroups();
        return ResponseEntity.ok(tagGroups);
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDTO> addTag(@RequestBody TagDTO tagDTO) {
        TagDTO addedTag = tagService.addTag(tagDTO);
        // Need to get user info from tagDTO or another source
        Users user = userRepository.findById(1L)  // Replace with actual user ID source
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity( "added", "tag: " + tagDTO.getTagName());
        return ResponseEntity.ok(addedTag);
    }

    @GetMapping("/search")
    public ResponseEntity<List<QueryWithAnswersDTO>> searchQueries(
            @RequestParam(required = false) String questionText,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String tagGroup,
            @RequestParam(required = false) String answer) {

        List<QueryWithAnswersDTO> result = queryService.searchQueries(questionText, tags, tagGroup, answer);
        Users user = userRepository.findById(1L)  // Replace with actual user ID source
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity( "searched", "queries with criteria");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    public ResponseEntity<List<QueryWithAnswersDTO>> searchQueriesByKeyword(@RequestBody SearchRequestDTO searchRequest) {
        List<QueryWithAnswersDTO> results = queryService.searchQueriesByKeyword(searchRequest.getKeyword());
        Users user = userRepository.findById(1L)  // Replace with actual user ID source
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityLogService.logActivity( "searched", "queries with keyword: " + searchRequest.getKeyword());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }
            Users user = userRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            queryService.processFile(file, user.getId());

            activityLogService.logActivity( "uploaded", "excel file: " + file.getOriginalFilename());
            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process the file: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try {
            queryService.processExcel(file);
            return ResponseEntity.ok("File uploaded and processed successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<List<String>> getCompanies() {
        // Mock data, replace this with a service call if you have a database or logic to fetch companies
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
