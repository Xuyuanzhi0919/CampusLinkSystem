package com.campuslink.dto.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新系统配置请求DTO
 */
@Data
public class UpdateConfigRequest {

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    @Size(max = 500, message = "配置说明最多500个字符")
    private String description;
}
