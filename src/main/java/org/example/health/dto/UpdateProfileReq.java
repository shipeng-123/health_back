package org.example.health.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

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

    // 头像地址（前端先传 base64 或 URL 都行；后面建议只传上传接口返回的 URL）
    private String avatarUrl;
}