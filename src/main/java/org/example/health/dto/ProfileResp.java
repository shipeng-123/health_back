package org.example.health.dto;

import lombok.Data;

@Data
public class ProfileResp {
    private Long id;
    private String username;
    private String phone;
    private String nickname;
    private Integer gender;
    private String avatarUrl;
    private String email;
}