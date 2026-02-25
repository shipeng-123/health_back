package org.example.health.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    private Long id;
    private String username;
    private String phone;
    private String password;
    private String nickname;
    private Integer gender;
    private String avatarUrl;
    private String email;
    private java.math.BigDecimal weight;
}