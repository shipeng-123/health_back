package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("sys_user")
public class SysUser {
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String nickname;
    private Integer gender;

    // 表字段 avatar_url，MP一般可自动映射，但我建议显式写，避免坑
    @TableField("avatar_url")
    private String avatarUrl;

    private String email;

    // 你已经有
    private BigDecimal weight;

    // ✅ 新增：身高 cm（表字段 height_cm）
    @TableField("height_cm")
    private BigDecimal heightCm;

    // ✅ 新增：目标体重（可选）
    @TableField("goal_weight_kg")
    private BigDecimal goalWeightKg;

    // ✅ 新增：活动水平 1低 2中 3高
    @TableField("activity_level")
    private Integer activityLevel;

    // ✅ 新增：目标类型 1减脂 2增肌 3维持
    @TableField("target_type")
    private Integer targetType;

    // ✅ 新增：出生日期
    @TableField("birth_date")
    private LocalDate birthDate;
}