package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WeekPlanItemResp {
    private Long id;
    private LocalDate planDate;
    private Integer dayOfWeek;

    private String sportType;
    private Integer targetDurationMin;
    private BigDecimal targetDistanceKm;
    private String remindTime;
    private String remark;

    private Integer done;
    private LocalDateTime doneTime;
}