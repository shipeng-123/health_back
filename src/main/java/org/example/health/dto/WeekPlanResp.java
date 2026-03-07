package org.example.health.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WeekPlanResp {
    private Long planId;
    private String planName;
    private LocalDate weekStartDate;

    private Integer totalCount;
    private Integer doneCount;
    private Double completionRate;

    private List<WeekPlanItemResp> items;
}