package com.queryapplication.controller;

import com.queryapplication.dto.DateGroupedLogsDTO;
import com.queryapplication.dto.ActivityLogDTO;
import com.queryapplication.exception.ResourceNotFoundException;
import com.queryapplication.service.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.queryapplication.response.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api/v1/queryapplication/logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<?> getLogs(@RequestParam(defaultValue = "0") int page) {
        try {
            List<DateGroupedLogsDTO> logs = activityLogService.getAllLogs(page);
            return ResponseEntity.ok(logs);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error accessing activity logs. Please try again later"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DateGroupedLogsDTO>> getUserLogs(
          @PathVariable Long userId,
          @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(activityLogService.getUserLogs(userId, page));
    }
}