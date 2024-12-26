package com.queryapplication.controller;

import com.queryapplication.dto.*;
import com.queryapplication.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/queryapplication/queries")
public class QueryController {

    private final QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
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

    // Delete all answers for a particular query
    @DeleteMapping("/{queryId}/answers")
    public void deleteAllAnswersForQuery(@PathVariable Long queryId) {
        queryService.deleteAllAnswersForQuery(queryId);
    }

    // Delete a particular query (and its answers)
    @DeleteMapping("/{queryId}")
    public void deleteQuery(@PathVariable Long queryId) {
        queryService.deleteQuery(queryId);
    }

    // Edit a particular query (question)
    @PutMapping("/{queryId}")
    public void editQuery(@PathVariable Long queryId, @RequestBody String newQuestion) {
        queryService.editQuery(queryId, newQuestion);
    }

    // Edit a particular answer
    @PutMapping("/answers/{answerId}")
    public void editAnswer(@PathVariable Long answerId, @RequestBody String newAnswerText) {
        queryService.editAnswer(answerId, newAnswerText);
    }

    // Copy a particular answer
    @PostMapping("/answers/{answerId}/copy")
    public void copyAnswer(@PathVariable Long answerId) {
        queryService.copyAnswer(answerId);
    }
}
