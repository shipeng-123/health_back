package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BodyMetricTrendResp {
    private List<LocalDate> dates;

    private List<BigDecimal> weightKg;
    private List<BigDecimal> bodyFatPct;
    private List<BigDecimal> waistCm;
    private List<BigDecimal> hipCm;
    private List<BigDecimal> chestCm;

    // 可选：如果你要 BMI 趋势，也可带上
    private List<BigDecimal> bmi;
}