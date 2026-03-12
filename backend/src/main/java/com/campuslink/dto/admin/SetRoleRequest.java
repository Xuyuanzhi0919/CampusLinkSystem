package com.campuslink.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改用户角色请求
 */
@Data
public class SetRoleRequest {
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "student|teacher|admin", message = "角色必须为 student/teacher/admin")
    private String role;
}
