package org.example.health.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeekPlanItemReq {

    @NotNull(message = "dayOfWeek不能为空")
    private Integer dayOfWeek; // 1-7

    @NotBlank(message = "sportType不能为空")
    private String sportType;

    @NotNull(message = "targetDurationMin不能为空")
    @Min(value = 1, message = "目标时长必须>0")
    private Integer targetDurationMin;

    private BigDecimal targetDistanceKm;

    private String remindTime; // HH:mm

    private String remark;
}