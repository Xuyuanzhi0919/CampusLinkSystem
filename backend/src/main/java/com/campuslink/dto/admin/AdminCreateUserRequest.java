package com.campuslink.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员手动创建用户请求
 */
@Data
public class AdminCreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度 3-30 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度至少 6 位")
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String studentId;

    /** 角色：student / teacher / admin，默认 student */
    private String role = "student";

    /** 初始积分，默认 100（与注册赠送保持一致） */
    private Integer initialPoints = 100;
}
