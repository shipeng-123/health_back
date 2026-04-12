package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    private Long id;

    private String username;

    private String phone;

    private String password;

    @TableField("role")
    private Integer role; // 0普通用户 1管理员

    private String nickname;

    private Integer gender;

    @TableField("avatar_url")
    private String avatarUrl;

    private String email;

    private BigDecimal weight;

    @TableField("height_cm")
    private BigDecimal heightCm;

    @TableField("goal_weight_kg")
    private BigDecimal goalWeightKg;

    @TableField("activity_level")
    private Integer activityLevel;

    @TableField("target_type")
    private Integer targetType;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("status")
    private Integer status; // 1启用 0禁用

    @TableField("deleted")
    private Integer deleted; // 0未删除 1已删除

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}