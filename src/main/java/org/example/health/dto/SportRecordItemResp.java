package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SportRecordItemResp {
    private Long id;
    private String sportType;
    private Integer durationMin;
    private BigDecimal distanceKm;
    private BigDecimal metValue;
    private BigDecimal calories;
    private LocalDate recordDate;
    private String remark;
    private java.time.LocalDateTime createTime;
}