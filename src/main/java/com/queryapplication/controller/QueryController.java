package com.queryapplication.controller;

import com.queryapplication.constants.ActivityConstants;
import com.queryapplication.dto.*;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.UserRepository;
import com.queryapplication.service.ActivityLogService;
import com.queryapplication.service.QueryService;
import com.queryapplication.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/queryapplication/queries")
public class QueryController {

    private final QueryService queryService;
    private final TagService tagService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    @Autowired
    public QueryController(QueryService queryService,
                           TagService tagService,
                           ActivityLogService activityLogService,
                           UserRepository userRepository) {
        this.queryService = queryService;
        this.tagService = tagService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    private Users getAuthenticatedUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<QueryDTO> getAllQueries(Authentication authentication) {
        List<QueryDTO> queries = queryService.getAllQueries();
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.QUERY_VIEWED,
                "all queries"
        );
        return queries;
    }

    @GetMapping("/{id}")
    public QueryDTO getQueryById(@PathVariable Long id, Authentication authentication) {
        QueryDTO query = queryService.getQueryById(id);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.QUERY_VIEWED,
                "query ID: " + id
        );
        return query;
    }

    @PostMapping
    public ResponseEntity<List<Long>> addQueries(@RequestBody List<NewQueryDTO> newQueries, Authentication authentication) {
        List<Long> queryIds = queryService.addQueries(newQueries);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.QUERY_ADDED,
                "queries with IDs: " + queryIds
        );
        return ResponseEntity.ok(queryIds);
    }

    @PostMapping("/id/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswers(
            @RequestBody List<NewAnswerDTO> newAnswers,
            Authentication authentication) {
        List<AnswerResponseDTO> response = queryService.addAnswers(newAnswers);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.ANSWER_ADDED,
                "new answers added"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queryId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> addAnswersToQuery(
            @PathVariable Long queryId,
            @RequestBody List<AnswerRequestDTO> newAnswers,
            Authentication authentication) {
        List<AnswerResponseDTO> response = queryService.addAnswersToQuery(queryId, newAnswers);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.ANSWER_ADDED,
                "answers to query ID: " + queryId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Long>> addBulkQueries(
            @RequestBody List<BulkQueryDTO> bulkQueries,
            Authentication authentication) {
        List<Long> queryIds = queryService.addBulkQueries(bulkQueries);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.QUERY_ADDED,
                "bulk queries added with IDs: " + queryIds
        );
        return ResponseEntity.ok(queryIds);
    }

    @DeleteMapping("/answers/{answerId}")
    public void deleteAnswer(@PathVariable Long answerId, Authentication authentication) {
        queryService.deleteAnswer(answerId);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.ANSWER_DELETED,
                "answer ID: " + answerId
        );
    }

    @DeleteMapping("/{queryId}/answers")
    public ResponseEntity<Void> deleteAnswersForQuery(
            @PathVariable Long queryId,
            Authentication authentication) {
        queryService.deleteAllAnswersForQuery(queryId);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.ANSWER_DELETED,
                "all answers for query ID: " + queryId
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{queryId}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long queryId, Authentication authentication) {
        QueryDTO queryDTO = queryService.getQueryById(queryId);
        queryService.deleteQuery(queryId);

        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.QUERY_DELETED,
                String.format("Query: %s (ID: %d)", queryDTO.getQuestion(), queryId)
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{queryId}")
    public void editQuery(
            @PathVariable Long queryId,
            @RequestBody List<NewQueryDTO> newQueryDetails,
            Authentication authentication) {
        if (newQueryDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of queries.");
        }
        NewQueryDTO newQueryDTO = newQueryDetails.get(0);
        QueryDTO oldQuery = queryService.getQueryById(queryId);
        queryService.editQuery(queryId, newQueryDTO);

        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.QUERY_EDITED,
                String.format("Query ID: %d from '%s' to '%s'",
                        queryId, oldQuery.getQuestion(), newQueryDTO.getQuestion())
        );
    }

    @PatchMapping("/answers/{answerId}")
    public void editAnswer(
            @PathVariable Long answerId,
            @RequestBody List<NewAnswerDTO> newAnswerDetails,
            Authentication authentication) {
        if (newAnswerDetails.isEmpty()) {
            throw new IllegalArgumentException("Request body should contain a list of answers.");
        }
        NewAnswerDTO newAnswerDTO = newAnswerDetails.get(0);
        queryService.editAnswer(answerId, newAnswerDTO);

        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.ANSWER_EDITED,
                "answer ID: " + answerId
        );
    }

    @PostMapping("/answers/{answerId}/copy")
    public void copyAnswer(@PathVariable Long answerId, Authentication authentication) {
        queryService.copyAnswer(answerId);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                ActivityConstants.ANSWER_COPIED,
                "copied answer ID: " + answerId
        );
    }

    @GetMapping("/tags/groups")
    public ResponseEntity<List<String>> getAllTagGroups() {
        return ResponseEntity.ok(tagService.getAllTagGroups());
    }

    @GetMapping("/tags/group/{tagGroup}")
    public ResponseEntity<List<TagDTO>> getTagsByGroup(@PathVariable String tagGroup) {
        return ResponseEntity.ok(tagService.getTagsByGroup(tagGroup));
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDTO> addTag(
            @RequestBody TagDTO tagDTO,
            Authentication authentication) {
        TagDTO addedTag = tagService.addTag(tagDTO);
        activityLogService.logActivity(
                getAuthenticatedUser(authentication),
                "added tag",
                "tag: " + tagDTO.getTagName()
        );
        return ResponseEntity.ok(addedTag);
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
    public ResponseEntity<List<QueryWithAnswersDTO>> searchQueriesByKeyword(
            @RequestBody SearchRequestDTO searchRequest) {
        List<QueryWithAnswersDTO> results = queryService.searchQueriesByKeyword(searchRequest.getKeyword());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }
            queryService.processFile(file);
            activityLogService.logActivity(
                    getAuthenticatedUser(authentication),
                    "uploaded excel",
                    "file: " + file.getOriginalFilename()
            );
            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process the file: " + e.getMessage());
        }
    }

    @PostMapping("/upload-file")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }
            queryService.processFileReader(file);
            activityLogService.logActivity(
                    getAuthenticatedUser(authentication),
                    "uploaded file",
                    "file: " + file.getOriginalFilename()
            );
            return ResponseEntity.ok("File processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to process the file: " + e.getMessage());
        }
    }
}