package com.campuslink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    /**
     * 旧密码
     */
    @Schema(description = "旧密码", example = "OldPass123")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "NewPass123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,20}$",
            message = "密码必须包含大小写字母和数字")
    private String newPassword;
}
