package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TodayPlanItemResp {
    private Long id;
    private Integer dayOfWeek;
    private String sportType;
    private Integer targetDurationMin;
    private BigDecimal targetDistanceKm;
    private String remindTime;
    private String remark;

    private Integer done; // 0/1
    private LocalDateTime doneTime;
}