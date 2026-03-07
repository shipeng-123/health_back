package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_sport_plan_item")
public class UserSportPlanItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long userId;

    private LocalDate planDate;

    private Integer dayOfWeek; // 1-7

    private String sportType;

    private Integer targetDurationMin;

    private BigDecimal targetDistanceKm;

    private String remindTime;

    private String remark;

    private Integer done; // 0/1

    private LocalDateTime doneTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}