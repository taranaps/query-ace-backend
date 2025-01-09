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

    public void processFile(MultipartFile file, Long userId) throws IOException {
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            String fileName = file.getOriginalFilename();

            if (fileName.startsWith("Bid")) {
                processBidFile(workbook, userId);
            } else if (fileName.startsWith("Qualitative")) {
                processQualitativeFile(workbook, userId);
            } else if (fileName.startsWith("Vendor")) {
                processVendorFile(workbook, userId);
            } else if (Character.isDigit(fileName.charAt(0))) {
                // New logic for files starting with numbers
                processNumberedFile(workbook, userId);
            }
            else if (fileName.startsWith("Experion Technologies (I)")) {
                processAjishFile(workbook, userId);
            }

            else {
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

    private void processAjishFile(Workbook workbook, Long userId) {
        // Get the first sheet
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            System.out.println("Error: Sheet is empty or does not exist.");
            return;
        }

        // Create an iterator for the rows
        Iterator<Row> rowIterator = sheet.iterator();

        // Skip the first row (header) and second row as per the requirement
        if (rowIterator.hasNext()) rowIterator.next();  // Skip the first row (header)
         // Skip the second row

        // Iterate over the remaining rows
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // Get the question from the second column (index 1)
            Cell questionCell = row.getCell(1);
            String question = "";
            if (questionCell != null && questionCell.getCellType() == CellType.STRING) {
                question = questionCell.getStringCellValue().trim();
                System.out.println("Found question: " + question);  // Log the question
            } else {
                System.out.println("Error: Question is missing or not a string at row " + row.getRowNum());
            }

            // Get the answers from the 6th (index 5) and 7th (index 6) columns
            String answer = "";
            Cell answerCell1 = row.getCell(5);
            if (answerCell1 != null && answerCell1.getCellType() == CellType.STRING) {
                answer = answerCell1.getStringCellValue().trim();
            }

            // If there is another answer in the 7th column (index 6), append it to the answer
            Cell answerCell2 = row.getCell(6);
            if (answerCell2 != null && answerCell2.getCellType() == CellType.STRING) {
                answer += " " + answerCell2.getStringCellValue().trim();
            }

            // If both question and answer are not empty, save them
            if (!question.isEmpty() && !answer.isEmpty()) {
                try {
                    saveQuestionAndAnswer(question, answer, userId);
                    System.out.println("Saved question and answer for row " + row.getRowNum());
                } catch (Exception e) {
                    System.out.println("Error: Unable to save question and answer for row " + row.getRowNum() + ". Exception: " + e.getMessage());
                }
            } else {
                System.out.println("Error: Skipping row " + row.getRowNum() + " because question or answer is empty.");
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

    private void processNumberedFile(Workbook workbook, Long userId) {
        // Process only sheets named "Questionnaire"
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().contains("Questionnaire")) {
                processQuestionnaireSheet(sheet, userId);
            }
        }
    }

    private void processQuestionnaireSheet(Sheet sheet, Long userId) {
        Iterator<Row> rowIterator = sheet.iterator();

        // Skip first 8 rows
        for (int i = 0; i < 8 && rowIterator.hasNext(); i++) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // Get question from column 9 (index 8)
            Cell questionCell = row.getCell(8);
            String question = "";
            if (questionCell != null && questionCell.getCellType() == CellType.STRING) {
                question = questionCell.getStringCellValue().trim();
            }

            if (!question.isEmpty()) {
                // Combine answers from columns 10, 12, and 13 (indices 9, 11, and 12)
                StringBuilder answer = new StringBuilder();

                // Column 10
                Cell answerCell1 = row.getCell(9);
                if (answerCell1 != null && answerCell1.getCellType() == CellType.STRING && hasWhiteBackground(answerCell1)) {
                    answer.append(answerCell1.getStringCellValue().trim());
                }

                // Column 12
                Cell answerCell2 = row.getCell(11);
                if (answerCell2 != null && answerCell2.getCellType() == CellType.STRING && hasWhiteBackground(answerCell2)) {
                    if (answer.length() > 0) answer.append(" ");
                    answer.append(answerCell2.getStringCellValue().trim());
                }

                // Column 13
                Cell answerCell3 = row.getCell(12);
                if (answerCell3 != null && answerCell3.getCellType() == CellType.STRING && hasWhiteBackground(answerCell3)) {
                    if (answer.length() > 0) answer.append(" ");
                    answer.append(answerCell3.getStringCellValue().trim());
                }

                // Save question and combined answer
                if (!question.isEmpty() && answer.length() > 0) {
                    saveQuestionAndAnswer(question, answer.toString(), userId);
                }
            }
        }
    }

    private boolean hasWhiteBackground(Cell cell) {
        // Check if the background color is white (indexed color code 9 is white)
        CellStyle style = cell.getCellStyle();
        short bgColor = style.getFillForegroundColor();
        return bgColor == 9;
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
