package org.example.health.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TodayPlanResp {
    private LocalDate date;
    private LocalDate weekStartDate;
    private Long planId;
    private String planName;

    private Integer totalCount;
    private Integer doneCount;
    private Double completionRate; // done/total

    private List<TodayPlanItemResp> items;
}