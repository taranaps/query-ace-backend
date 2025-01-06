package com.queryapplication.controller;

import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;
import com.queryapplication.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/queryapplication/generatereport")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/search")
    public ResponseEntity<List<Answer>> searchAnswersByKeywords(@RequestBody List<String> keywords) {
        List<Answer> matchingAnswers = fileService.findAnswersByKeywords(keywords);
        return ResponseEntity.ok(matchingAnswers);
    }
}
