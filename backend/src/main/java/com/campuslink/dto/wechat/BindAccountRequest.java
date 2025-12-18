package com.campuslink.dto.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 绑定已有账号请求
 */
@Data
@Schema(description = "绑定已有账号请求")
public class BindAccountRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", required = true, example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true, example = "password123")
    private String password;
}
