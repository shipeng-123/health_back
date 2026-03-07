package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_sport_plan")
public class UserSportPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate weekStartDate; // 周一

    private String name;

    private Integer sourceType; // 1模板 2自定义

    private Long templateId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}