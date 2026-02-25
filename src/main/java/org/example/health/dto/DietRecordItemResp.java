package org.example.health.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DietRecordItemResp {
    private Long id;
    private Long foodItemId;
    private String foodName;
    private Integer mealType;
    private BigDecimal intakeGram;
    private BigDecimal caloriePer100g;
    private BigDecimal totalCalories;
    private String remark;
    private LocalDateTime createTime;
}