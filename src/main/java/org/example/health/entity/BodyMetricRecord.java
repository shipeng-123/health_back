package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("body_metric_record")
public class BodyMetricRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    private BigDecimal weightKg;
    private BigDecimal bodyFatPct;

    private BigDecimal chestCm;
    private BigDecimal waistCm;
    private BigDecimal hipCm;

    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}