package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BodyMetricResp {
    private Long id;
    private LocalDate recordDate;

    private BigDecimal weightKg;
    private BigDecimal bodyFatPct;
    private BigDecimal chestCm;
    private BigDecimal waistCm;
    private BigDecimal hipCm;

    // 计算字段
    private BigDecimal bmi;
    private String bmiLevel;     // 健康评估：偏瘦/正常/超重/肥胖
    private String bmiAdvice;    // 建议
}