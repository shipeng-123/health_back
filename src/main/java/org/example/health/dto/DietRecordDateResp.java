package org.example.health.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DietRecordDateResp {
    private LocalDate date;
    private Integer recordCount;
    private BigDecimal totalCalories;

    // 也可以后续改成按餐次分组，这里先简单返回列表
    private List<DietRecordItemResp> records;
}