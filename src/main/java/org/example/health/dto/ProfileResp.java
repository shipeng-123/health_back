package org.example.health.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProfileResp {
    private Long id;
    private String username;
    private String phone;
    private String nickname;
    private Integer gender;
    private String avatarUrl;
    private String email;

    // ✅ 身体资料
    private BigDecimal heightCm;       // 身高(cm)
    private BigDecimal weight;         // 当前体重(kg) - 你DB列就是 weight
    private BigDecimal goalWeightKg;   // 目标体重(kg)
    private Integer activityLevel;     // 1低 2中 3高
    private Integer targetType;        // 1减脂 2增肌 3维持
    private LocalDate birthDate;       // 出生日期
}