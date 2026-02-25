package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sport_record")
public class SportRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String sportType;

    private Integer durationMin;

    private BigDecimal distanceKm;

    private BigDecimal metValue;

    private BigDecimal calories;

    private LocalDate recordDate;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}