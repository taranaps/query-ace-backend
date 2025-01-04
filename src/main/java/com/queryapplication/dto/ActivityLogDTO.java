package com.queryapplication.dto;

import com.queryapplication.entity.Users;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDTO {
    private String logDate;
    private String timestamp;
    private String logDescription;
}
