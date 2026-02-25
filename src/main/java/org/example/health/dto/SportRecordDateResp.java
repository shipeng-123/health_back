package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SportRecordDateResp {
    private LocalDate date;
    private Integer recordCount;
    private Integer totalDurationMin;
    private BigDecimal totalCalories;
    private List<SportRecordItemResp> records;
}