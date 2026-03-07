package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sport_plan_template_item")
public class SportPlanTemplateItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private Integer dayOfWeek; // 1-7

    private String sportType;

    private Integer targetDurationMin;

    private BigDecimal targetDistanceKm;

    private String remindTime; // HH:mm

    private String remark;

    private Integer sortNo;
}