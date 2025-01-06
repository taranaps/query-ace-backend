package com.queryapplication.service;

import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;

import java.util.List;

public interface FileService {
    public List<Answer> findAnswersByKeywords(List<String> keywords);


    }
