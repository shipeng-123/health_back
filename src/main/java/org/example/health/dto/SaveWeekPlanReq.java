package org.example.health.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SaveWeekPlanReq {

    @NotNull(message = "weekStartDate不能为空")
    private LocalDate weekStartDate; // 周一

    @Size(min = 1, max = 100, message = "计划名称长度1-100")
    private String name;

    @Valid
    private List<WeekPlanItemReq> items;
}