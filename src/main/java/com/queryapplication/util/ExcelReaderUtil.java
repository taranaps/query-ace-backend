package com.queryapplication.util;

import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;
import com.queryapplication.entity.Users;
import com.queryapplication.repository.AnswerRepository;
import com.queryapplication.repository.QueryRepository;
import com.queryapplication.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Component
public class ExcelReaderUtil {

    private final QueryRepository queryRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository usersRepository;

    @Autowired
    public ExcelReaderUtil(QueryRepository queryRepository, AnswerRepository answerRepository, UserRepository usersRepository) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
        this.usersRepository = usersRepository;
    }

    public void processExcel(MultipartFile file, Long userId) throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            String fileName = file.getOriginalFilename();

            if (fileName.startsWith("Bid")) {
                processBidFile(workbook, userId);
            } else if (fileName.startsWith("Qualitative")) {
                processQualitativeFile(workbook, userId);
            } else if (fileName.startsWith("Vendor")) {
                processVendorFile(workbook, userId);  // Process Vendor file
            } else {
                int sheetCount = workbook.getNumberOfSheets();
                if (sheetCount > 10) {
                    processAllSheets(workbook, userId);
                } else if (sheetCount > 0) {
                    Sheet lastSheet = workbook.getSheetAt(sheetCount - 1);
                    processSheet(lastSheet, userId);
                } else {
                    System.out.println("The workbook has no sheets to process.");
                }
            }
        }
    }

    private void processBidFile(Workbook workbook, Long userId) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // Skip the first row
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell questionCell = row.getCell(1);
            String question = questionCell != null && questionCell.getCellType() == CellType.STRING ? questionCell.getStringCellValue().trim() : "";
            String answer = combineAnswerParts(row, 2, 3);
            saveQuestionAndAnswer(question, answer, userId);
        }
    }

    private void processQualitativeFile(Workbook workbook, Long userId) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() >= 2) {
                Cell questionCell = row.getCell(1);
                String question = questionCell != null && questionCell.getCellType() == CellType.STRING ? questionCell.getStringCellValue().trim() : "";
                String answer = combineAnswerParts(row, 2, 3);
                saveQuestionAndAnswer(question, answer, userId);
            }
        }
    }

    private void processVendorFile(Workbook workbook, Long userId) {

        Sheet sheet = workbook.getSheetAt(1);
        Iterator<Row> rowIterator = sheet.iterator();


        for (int i = 0; i < 13 && rowIterator.hasNext(); i++) {
            rowIterator.next();
        }


        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Cell cell = row.getCell(3);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue().trim();


                if (cellValue.startsWith("Vendor")) {
                    String answer = combineAnswerParts(row, 4, 5);
                    saveQuestionAndAnswer(cellValue, answer, userId);
                }
            }
        }
    }

    private void processAllSheets(Workbook workbook, Long userId) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            processSheet(sheet, userId);
        }
    }

    private void processSheet(Sheet sheet, Long userId) {
        Iterator<Row> rowIterator = sheet.iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().trim();
                    if (cellValue.endsWith("?")) {
                        String question = cellValue;
                        String answer = combineAnswerParts(row, i + 1, i + 2);
                        if (answer.isEmpty()) {
                            answer = "No answer yet";
                        }
                        saveQuestionAndAnswer(question, answer, userId);
                    }
                }
            }
        }
    }

    private String combineAnswerParts(Row row, int part1Index, int part2Index) {
        StringBuilder answer = new StringBuilder();

        Cell part1 = row.getCell(part1Index);
        Cell part2 = row.getCell(part2Index);

        if (part1 != null && part1.getCellType() == CellType.STRING) {
            answer.append(part1.getStringCellValue().trim());
        }
        if (part2 != null && part2.getCellType() == CellType.STRING) {
            if (answer.length() > 0) {
                answer.append(" ");
            }
            answer.append(part2.getStringCellValue().trim());
        }

        return answer.toString();
    }

    private void saveQuestionAndAnswer(String question, String answer, Long userId) {
        Users user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Query query = new Query();
        query.setQuestion(question);
        query.setAddedBy(user);
        query = queryRepository.save(query);

        Answer answerEntity = new Answer();
        answerEntity.setAnswer(answer);
        answerEntity.setQuery(query);
        answerEntity.setAddedBy(user);

        answerRepository.save(answerEntity);

        System.out.println("Saved question: " + question + " with answer: " + answer);
    }
}
