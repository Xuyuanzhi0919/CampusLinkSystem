package com.campuslink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * 访问 Token
     */
    private String token;

    /**
     * 刷新 Token
     */
    private String refreshToken;

    /**
     * 用户信息
     */
    private UserVO user;
}
