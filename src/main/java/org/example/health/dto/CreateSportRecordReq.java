package org.example.health.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSportRecordReq {

    @NotBlank(message = "运动类型不能为空")
    private String sportType;

    @NotNull(message = "运动时长不能为空")
    @Min(value = 1, message = "运动时长必须大于0")
    private Integer durationMin;

    // 可选：跑步/骑行可填
    private BigDecimal distanceKm;

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    private String remark;
}