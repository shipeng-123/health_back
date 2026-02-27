package org.example.health.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateProfileReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度需在1-20位之间")
    private String nickname;

    // 0未知 1男 2女
    private Integer gender;

    @Email(message = "邮箱格式不正确")
    private String email;

    // 头像地址
    private String avatarUrl;

    // ✅ 身体资料（都允许为空：注册后可慢慢补齐）
    private BigDecimal heightCm;       // cm
    private BigDecimal weight;         // kg（对应 sys_user.weight）
    private BigDecimal goalWeightKg;   // kg
    private Integer activityLevel;     // 1低 2中 3高
    private Integer targetType;        // 1减脂 2增肌 3维持
    private LocalDate birthDate;       // yyyy-MM-dd
}