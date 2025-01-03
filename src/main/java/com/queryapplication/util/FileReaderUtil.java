package com.queryapplication.util;

import com.queryapplication.entity.*;
import com.queryapplication.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class FileReaderUtil {

    private final QueryRepository queryRepository;
    private final AnswerRepository answerRepository;
    private final TagGroupRepository tagGroupRepository;
    private final TagRepository tagRepository;

    @Autowired
    public FileReaderUtil(QueryRepository queryRepository, AnswerRepository answerRepository,
                          TagGroupRepository tagGroupRepository, TagRepository tagRepository) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
        this.tagGroupRepository = tagGroupRepository;
        this.tagRepository = tagRepository;
    }

    public void processExcel(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // Assuming data is on the first sheet

        Map<String, Query> queryMap = new HashMap<>();
        Map<Integer, String> tagGroupMap = new HashMap<>();

        // Read header row for tag group names
        Row headerRow = sheet.getRow(0);
        for (int colIndex = 2; colIndex < headerRow.getLastCellNum(); colIndex++) {
            Cell headerCell = headerRow.getCell(colIndex);
            if (headerCell != null && headerCell.getCellType() == CellType.STRING) {
                tagGroupMap.put(colIndex, headerCell.getStringCellValue().trim());
            }
        }

        // Step 1: Handle answers and save queries
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Skip the header row
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            // Handle answers first for this row
            handleAnswers(sheet, rowIndex, queryMap);
        }

        // Step 2: Process tags after all queries and answers are saved
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            // Process tags for the current question
            processTags(sheet, row, tagGroupMap, queryMap);
        }

        workbook.close();
    }

    private void handleAnswers(Sheet sheet, int rowIndex, Map<String, Query> queryMap) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) return;

        // Answer is located in the second column (index 1)
        Cell answerCell = row.getCell(1); // Answer cell
        if (answerCell != null && answerCell.getCellType() == CellType.STRING) {
            String answer = answerCell.getStringCellValue().trim();
            if (!answer.isEmpty()) {
                // Get or create the corresponding query for this answer
                String question = row.getCell(0).getStringCellValue().trim();
                Query query = queryMap.computeIfAbsent(question, this::saveQuery);

                // Save the answer
                saveAnswer(query, answer);
            }
        }
    }

    private void processTags(Sheet sheet, Row row, Map<Integer, String> tagGroupMap, Map<String, Query> queryMap) {
        // Iterate through the tag columns and save each tag for the current question
        for (Map.Entry<Integer, String> entry : tagGroupMap.entrySet()) {
            int colIndex = entry.getKey();
            String tagGroupName = entry.getValue();
            TagGroup tagGroup = getOrCreateTagGroup(tagGroupName);

            // Process tags if there is a corresponding value
            Cell tagCell = row.getCell(colIndex);
            if (tagCell != null && tagCell.getCellType() == CellType.STRING) {
                String tagName = tagCell.getStringCellValue().trim();
                if (!tagName.isEmpty()) {
                    String question = row.getCell(0).getStringCellValue().trim();
                    Query query = queryMap.get(question); // Get the query associated with the question
                    if (query != null) {
                        saveTag(query, tagGroup, tagName);
                    }
                }
            }
        }
    }

    private boolean isMergedRegion(Sheet sheet, int rowIndex, int colIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowIndex, colIndex)) {
                return true;
            }
        }
        return false;
    }

    private Query saveQuery(String question) {
        Query query = new Query();
        query.setQuestion(question);
        return queryRepository.save(query);
    }

    private void saveAnswer(Query query, String answerText) {
        Answer answer = new Answer();
        answer.setQuery(query);
        answer.setAnswer(answerText);
        answerRepository.save(answer);
    }

    private TagGroup getOrCreateTagGroup(String tagGroupName) {
        return tagGroupRepository.findByName(tagGroupName)
                .orElseGet(() -> {
                    TagGroup tagGroup = new TagGroup();
                    tagGroup.setName(tagGroupName);
                    return tagGroupRepository.save(tagGroup);
                });
    }

    private void saveTag(Query query, TagGroup tagGroup, String tagName) {
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
