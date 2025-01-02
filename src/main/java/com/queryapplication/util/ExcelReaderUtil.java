package com.queryapplication.util;




import com.queryapplication.entity.Answer;
import com.queryapplication.entity.Query;
import com.queryapplication.repository.AnswerRepository;
import com.queryapplication.repository.QueryRepository;
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

    @Autowired
    public ExcelReaderUtil(QueryRepository queryRepository, AnswerRepository answerRepository) {
        this.queryRepository = queryRepository;
        this.answerRepository = answerRepository;
    }

    public void processExcel(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);

        int sheetCount = workbook.getNumberOfSheets();
        if (sheetCount > 0) {
            // Get the last sheet in the workbook
            Sheet lastSheet = workbook.getSheetAt(sheetCount - 1);
            System.out.println("Processing the last sheet: " + lastSheet.getSheetName());
            processSheet(lastSheet);
        } else {
            System.out.println("The workbook has no sheets to process.");
        }

        workbook.close();
    }

    private void processSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String cellValue = cell.getStringCellValue().trim();
                    if (cellValue.endsWith("?")) {
                        // Found a question
                        String question = cellValue;
                        String answer = combineAnswerParts(row, i + 1, i + 2); // Combine next two columns as answer
                        if (answer.isEmpty()) {
                            answer = "No answer yet"; // Default answer if none found
                        }
                        saveQuestionAndAnswer(question, answer);
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
