package com.queryapplication.util;

import com.queryapplication.entity.*;
import com.queryapplication.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class CategoryCompanyExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(CategoryCompanyExcelUtil.class);

    private final QueryRepository queryRepository;
    private final AnswerRepository answerRepository;
    private final TagGroupRepository tagGroupRepository;
    private final TagRepository tagRepository;
    private final UserRepository usersRepository;

    @Autowired
    public CategoryCompanyExcelUtil(QueryRepository queryRepository, AnswerRepository answerRepository,
                                    TagGroupRepository tagGroupRepository, TagRepository tagRepository, UserRepository usersRepository) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
        this.tagGroupRepository = tagGroupRepository;
        this.tagRepository = tagRepository;
        this.usersRepository = usersRepository;
    }

    public void processExcel(MultipartFile file, Long userId) throws IOException {
        logger.info("Starting processExcel method");
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Map<String, Query> queryMap = new HashMap<>();
        Map<Integer, String> tagGroupMap = new HashMap<>();

        Row headerRow = sheet.getRow(0);
        for (int colIndex = 2; colIndex < headerRow.getLastCellNum(); colIndex++) {
            Cell headerCell = headerRow.getCell(colIndex);
            if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                tagGroupMap.put(colIndex, headerCell.getStringCellValue().trim());
                logger.info("Tag group detected: {}", headerCell.getStringCellValue().trim());
            }
        }


        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                Cell questionCell = row.getCell(0);
                if (questionCell != null && questionCell.getCellType() == CellType.STRING) {
                    String question = questionCell.getStringCellValue().trim();
                    if (!question.isEmpty()) {
                        queryMap.computeIfAbsent(question, q -> saveQuery(q, userId));
                        logger.info("Saved question: {}", question);
                    }
                }
            }
        }


        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                handleAnswers(row, queryMap, userId);
            }
        }


        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                processTags(row, tagGroupMap, queryMap);
            }
        }

        workbook.close();
        logger.info("Excel processing completed successfully");
    }

    private void handleAnswers(Row row, Map<String, Query> queryMap, Long userId) {
        Cell questionCell = row.getCell(0);
        Cell answerCell = row.getCell(1);

        if (questionCell != null && questionCell.getCellType() == CellType.STRING) {
            String question = questionCell.getStringCellValue().trim();
            if (!question.isEmpty()) {
                Query query = queryMap.get(question);

                if (answerCell != null && answerCell.getCellType() == CellType.STRING) {
                    String answer = answerCell.getStringCellValue().trim();
                    if (!answer.isEmpty()) {
                        saveAnswer(query, answer, userId);
                        logger.info("Saved answer '{}' for question '{}'.", answer, question);
                    }
                }
            }
        }
    }

    private void processTags(Row row, Map<Integer, String> tagGroupMap, Map<String, Query> queryMap) {
        Cell questionCell = row.getCell(0);
        if (questionCell != null && questionCell.getCellType() == CellType.STRING) {
            String question = questionCell.getStringCellValue().trim();
            if (!question.isEmpty()) {
                Query query = queryMap.get(question);
                if (query != null) {
                    for (Map.Entry<Integer, String> entry : tagGroupMap.entrySet()) {
                        int colIndex = entry.getKey();
                        String tagGroupName = entry.getValue();
                        TagGroup tagGroup = getOrCreateTagGroup(tagGroupName);

                        Cell tagCell = row.getCell(colIndex);
                        if (tagCell != null && tagCell.getCellType() == CellType.STRING) {
                            String tagName = tagCell.getStringCellValue().trim();
                            if (!tagName.isEmpty()) {
                                saveTag(query, tagGroup, tagName);
                                logger.info("Saved tag '{}' under group '{}' for question '{}'.", tagName, tagGroupName, question);
                            }
                        }
                    }
                }
            }
        }
    }

    private Query saveQuery(String question, Long userId) {
        logger.debug("Saving new query: {}", question);
        Users user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Query query = new Query();
        query.setQuestion(question);
        query.setAddedBy(user);
        return queryRepository.save(query);
    }

    private void saveAnswer(Query query, String answerText, Long userId) {
        logger.debug("Saving answer for query '{}': {}", query.getQuestion(), answerText);
        Users user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Answer answer = new Answer();
        answer.setQuery(query);
        answer.setAnswer(answerText);
        answer.setAddedBy(user);
        answerRepository.save(answer);
    }

    private TagGroup getOrCreateTagGroup(String tagGroupName) {
        logger.debug("Fetching or creating tag group: {}", tagGroupName);
        return tagGroupRepository.findByName(tagGroupName)
                .orElseGet(() -> {
                    TagGroup tagGroup = new TagGroup();
                    tagGroup.setName(tagGroupName);
                    return tagGroupRepository.save(tagGroup);
                });
    }

    private void saveTag(Query query, TagGroup tagGroup, String tagName) {
        logger.debug("Saving tag '{}' under group '{}' for question '{}'.", tagName, tagGroup.getName(), query.getQuestion());
        Tag tag = tagRepository.findByTagNameAndTagGroup(tagName, tagGroup)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setTagName(tagName);
                    newTag.setTagGroup(tagGroup);
                    return tagRepository.save(newTag);
                });
        query.getTags().add(tag);
        queryRepository.save(query);
    }
}