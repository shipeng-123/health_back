package org.example.health.entity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DietRecord {
    private Long id;
    private Long userId;
    private Long foodItemId;
    private Integer mealType;
    private BigDecimal intakeGram;
    @TableField("calorie_per_100g")
    private BigDecimal caloriePer100g;
    private BigDecimal totalCalories;
    private LocalDate recordDate;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}