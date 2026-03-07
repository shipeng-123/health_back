package org.example.health.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanStatResp {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalItems;
    private Integer doneItems;
    private Double completionRate;
}