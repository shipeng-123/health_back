package org.example.health.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpsertBodyMetricReq {

    @NotNull
    private LocalDate recordDate;

    @DecimalMin(value = "0.01", message = "体重必须>0")
    private BigDecimal weightKg;

    // 允许为空，但如果填了就应合理
    @DecimalMin(value = "0.00", message = "体脂率不能<0")
    private BigDecimal bodyFatPct;

    @DecimalMin(value = "0.00", message = "胸围不能<0")
    private BigDecimal chestCm;

    @DecimalMin(value = "0.00", message = "腰围不能<0")
    private BigDecimal waistCm;

    @DecimalMin(value = "0.00", message = "臀围不能<0")
    private BigDecimal hipCm;

    private String remark;
}