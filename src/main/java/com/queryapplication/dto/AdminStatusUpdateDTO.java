package com.queryapplication.dto;

import com.queryapplication.entity.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminStatusUpdateDTO {

    @NotNull(message = "Admin ID is required")
    private Long id;

    @NotNull(message = "Status is required")
    private Status status;
}
