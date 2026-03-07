package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanTemplateItemResp {
    private Integer dayOfWeek; // 1-7
    private String sportType;
    private Integer targetDurationMin;
    private BigDecimal targetDistanceKm;
    private String remindTime;
    private String remark;
}