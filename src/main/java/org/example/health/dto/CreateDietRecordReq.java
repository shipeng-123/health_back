package org.example.health.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateDietRecordReq {

    @NotNull(message = "食物ID不能为空")
    private Long foodItemId;

    /**
     * 1早餐 2午餐 3晚餐 4加餐
     */
    @NotNull(message = "餐次不能为空")
    @Min(value = 1, message = "餐次不合法")
    @Max(value = 4, message = "餐次不合法")
    private Integer mealType;

    @NotNull(message = "摄入重量不能为空")
    @DecimalMin(value = "1.0", message = "摄入重量必须大于0")
    private Double intakeGram;

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    @Size(max = 255, message = "备注不能超过255字")
    private String remark;
}