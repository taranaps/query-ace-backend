package com.queryapplication.dto;

import lombok.Data;

@Data
public class TrendingQueryDTO {
    private Long id;
    private String question;
    private String createdAt;
    private Integer highestCopyCount;  // For tracking trending statu
}