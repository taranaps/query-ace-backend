package com.queryapplication.service;

import com.queryapplication.dto.*;

import java.util.List;

public interface QueryService {
    List<QueryDTO> getAllQueries();
    QueryDTO getQueryById(Long id);
    List<QueryWithAnswersDTO> getAllQueriesWithAnswers();
    QueryWithAnswersDTO getQueryWithAnswersById(Long id);
    void addQueries(List<NewQueryDTO> newQueries);
    void addAnswers(List<NewAnswerDTO> newAnswers);

    void deleteAnswer(Long answerId);

    void deleteAllAnswersForQuery(Long queryId);

    void deleteQuery(Long queryId);

    void editQuery(Long queryId, String newQuestion);

    void editAnswer(Long answerId, String newAnswerText);

    void copyAnswer(Long answerId);
}
