package com.queryapplication.service;

import com.queryapplication.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface QueryService {
    List<QueryDTO> getAllQueries();
    QueryDTO getQueryById(Long id);
    List<QueryWithAnswersDTO> getAllQueriesWithAnswers();
    QueryWithAnswersDTO getQueryWithAnswersById(Long id);
    List<Long> addQueries(List<NewQueryDTO> newQueries);
    List<AnswerResponseDTO> addAnswers(List<NewAnswerDTO> newAnswers);

    void deleteAnswer(Long answerId);

    void deleteAllAnswersForQuery(Long queryId);

    void deleteQuery(Long queryId);

    void editQuery(Long queryId, NewQueryDTO newQueryDTO);

    void editAnswer(Long answerId, NewAnswerDTO newAnswerDTO);

    void copyAnswer(Long answerId);

    List<QueryWithAnswersDTO> searchQueries(String questionText, List<String> tags, String tagGroup, String answer);

    List<QueryWithAnswersDTO> searchQueriesByKeyword(String keyword);
    void processFile(MultipartFile file) throws IOException;
}
