package org.example.health.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank(message = "账号或手机号不能为空")
    private String username; // 这里先沿用字段名，实际含义是“账号或手机号”

    @NotBlank(message = "密码不能为空")
    private String password;
}