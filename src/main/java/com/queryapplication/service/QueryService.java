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
}
