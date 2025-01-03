package com.queryapplication.util;

import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;
import com.queryapplication.repository.AnswerRepository;
import com.queryapplication.repository.QueryRepository;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DocReaderUtil {

    private final QueryRepository queryRepository;
    private final AnswerRepository answerRepository;

    @Autowired
    public DocReaderUtil(QueryRepository queryRepository, AnswerRepository answerRepository) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
    }

    public void processDocFile(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();

        if (fileName != null && fileName.endsWith(".doc")) {
            HWPFDocument document = new HWPFDocument(inputStream);
            WordExtractor extractor = new WordExtractor(document);
            String[] paragraphs = extractor.getParagraphText();
            parseAndSaveData(paragraphs);
            extractor.close();
            document.close();
        } else if (fileName != null && fileName.endsWith(".docx")) {
            // For .docx files, use XWPFDocument
            XWPFDocument document = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String[] paragraphs = extractor.getText().split("\n");
            parseAndSaveData(paragraphs);
            extractor.close();
            document.close();
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only .doc and .docx are supported.");
        }
    }

    private void parseAndSaveData(String[] paragraphs) {
        String currentQuestion = null;
        StringBuilder currentAnswer = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            if (paragraph.matches("^\\d+\\.\\s?.*\\?$")) {
                if (currentQuestion != null) {
                    saveQuestionAndAnswer(currentQuestion, currentAnswer.toString().trim());
                }
                currentQuestion = paragraph;
                currentAnswer.setLength(0);
            } else {
                if (currentAnswer.length() > 0) {
                    currentAnswer.append(" ");
                }
                currentAnswer.append(paragraph);
            }
        }

        if (currentQuestion != null) {
            saveQuestionAndAnswer(currentQuestion, currentAnswer.toString().trim());
        }
    }

    private void saveQuestionAndAnswer(String question, String answer) {
        Query query = new Query();
        query.setQuestion(question);
        query = queryRepository.save(query);

        Answer answerEntity = new Answer();
        answerEntity.setAnswer(answer);
        answerEntity.setQuery(query);

        answerRepository.save(answerEntity);

        System.out.println("Saved question: " + question + " with answer: " + answer);
    }
}
