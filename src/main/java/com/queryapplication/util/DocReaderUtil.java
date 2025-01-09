package com.queryapplication.util;

import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.AnswerRepository;
import com.queryapplication.repository.QueryRepository;
import com.queryapplication.repository.UserRepository;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class DocReaderUtil {

    private final QueryRepository queryRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Autowired
    public DocReaderUtil(QueryRepository queryRepository, AnswerRepository answerRepository, UserRepository userRepository) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Processes a .doc or .docx file, extracts questions and answers, and saves them into the database.
     *
     * @param file   the uploaded file
     * @param userId the ID of the user importing the file
     * @throws IOException if there is an issue reading the file
     */
    public void processDocFile(MultipartFile file, Long userId) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is invalid or null.");
        }

        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        try (InputStream inputStream = file.getInputStream()) {
            if (fileName.endsWith(".doc")) {
                processDocFile(inputStream, user);
            } else if (fileName.endsWith(".docx")) {
                processDocxFile(inputStream, user);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Only .doc and .docx are supported.");
            }
        } catch (Exception e) {
            throw new IOException("Error while processing file: " + fileName, e);
        }
    }

    private void processDocFile(InputStream inputStream, Users user) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            String[] paragraphs = extractor.getParagraphText();
            System.out.println("Extracted paragraphs from .doc file:");
            for (String paragraph : paragraphs) {
                System.out.println(paragraph);
            }
            parseAndSaveData(paragraphs, user);
        }
    }

    private void processDocxFile(InputStream inputStream, Users user) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String[] paragraphs = extractor.getText().split("\n");
            System.out.println("Extracted paragraphs from .docx file:");
            for (String paragraph : paragraphs) {
                System.out.println(paragraph);
            }
            parseAndSaveData(paragraphs, user);
        }
    }

    /**
     * Parses paragraphs to identify questions and answers and saves them into the database.
     *
     * @param paragraphs the paragraphs extracted from the document
     * @param user       the user importing the file
     */
    private void parseAndSaveData(String[] paragraphs, Users user) {
        String currentQuestion = null;
        StringBuilder currentAnswer = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            // Check if the paragraph is a question (starts with a number and ends with 'Single line text.')
            if (paragraph.matches("^\\d+\\.\\s?.*Single line text\\.$")) {
                String extractedQuestion = extractQuestionText(paragraph);
                // Save the previous question and answer if present
                if (currentQuestion != null) {
                    saveQuestionAndAnswer(currentQuestion, currentAnswer.toString().trim(), user);
                }
                currentQuestion = extractedQuestion;
                currentAnswer.setLength(0); // Reset the answer builder
            } else {
                // Append to the current answer
                if (currentAnswer.length() > 0) {
                    currentAnswer.append(" ");
                }
                currentAnswer.append(paragraph);
            }
        }

        // Save the last question and answer
        if (currentQuestion != null) {
            saveQuestionAndAnswer(currentQuestion, currentAnswer.toString().trim(), user);
        }
    }

    /**
     * Extracts the question text from a paragraph by removing the number and 'Single line text.'
     *
     * @param paragraph the paragraph containing the question
     * @return the extracted question text
     */
    private String extractQuestionText(String paragraph) {
        // Regex to match and capture text between the number and 'Single line text.'
        String regex = "^\\d+\\.\\s?(.*)\\sSingle line text\\.$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(paragraph);

        if (matcher.find()) {
            return matcher.group(1).trim(); // Return the captured group (question text)
        }
        return paragraph; // Fallback: return the entire paragraph if regex fails
    }

    /**
     * Saves a question and its corresponding answer to the database.
     *
     * @param question the question text
     * @param answer   the answer text
     * @param user     the user importing the file
     */
    private void saveQuestionAndAnswer(String question, String answer, Users user) {
        try {
            Query query = new Query();
            query.setQuestion(question);
            query.setAddedBy(user); // Associate the query with the user
            query = queryRepository.save(query); // Save question to DB

            if (query.getId() == null) {
                throw new RuntimeException("Failed to save query to the database.");
            }

            Answer answerEntity = new Answer();
            answerEntity.setAnswer(answer);
            answerEntity.setQuery(query);
            answerEntity.setAddedBy(user); // Associate the answer with the user

            answerRepository.save(answerEntity); // Save answer to DB
            System.out.println("Saved question: " + question + " with answer: " + answer);
        } catch (Exception e) {
            System.err.println("Error saving question: " + question + " with answer: " + answer);
            e.printStackTrace();
        }
    }
}
