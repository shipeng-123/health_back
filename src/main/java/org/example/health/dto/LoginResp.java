package org.example.health.dto;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private String username;
    private String nickname;
}