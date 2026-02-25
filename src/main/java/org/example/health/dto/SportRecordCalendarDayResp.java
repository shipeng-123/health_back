package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SportRecordCalendarDayResp {
    private LocalDate date;
    private Integer recordCount;
    private Integer totalDurationMin;
    private BigDecimal totalCalories;
}