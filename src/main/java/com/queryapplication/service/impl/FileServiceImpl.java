package com.queryapplication.service.impl;

import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;
import com.queryapplication.repository.AnswerRepository;
import com.queryapplication.repository.QueryRepository;
import com.queryapplication.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public List<Answer> findAnswersByKeywords(List<String> keywords) {
        List<Answer> allAnswers = answerRepository.findAll(); // Fetch all answers

        List<Answer> matchedAnswers = new ArrayList<>();
        int keywordCount = keywords.size();

        while (keywordCount > 0 && matchedAnswers.isEmpty()) {
            final int currentKeywordCount = keywordCount;
            matchedAnswers = allAnswers.stream()
                    .filter(answer -> countKeywordMatches(answer, keywords) == currentKeywordCount)
                    .sorted(Comparator.comparingInt(Answer::getCopyCount).reversed()) // Sort by copyCount (descending)
                    .collect(Collectors.toList());
            keywordCount--; // Reduce the keyword count if no match is found
        }

        return matchedAnswers;
    }

    private int countKeywordMatches(Answer answer, List<String> keywords) {
        return (int) keywords.stream()
                .filter(keyword -> answer.getAnswer().toLowerCase().contains(keyword.toLowerCase()))
                .count();
    }
    private List<Answer> filterAnswersByKeywords(Query query, List<String> keywords) {
        return query.getAnswers().stream()
                .filter(answer -> keywords.stream()
                        .allMatch(keyword -> answer.getAnswer().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }
}


