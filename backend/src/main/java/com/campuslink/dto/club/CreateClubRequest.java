package com.campuslink.dto.club;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建社团请求
 */
@Data
public class CreateClubRequest {
    /**
     * 社团名称
     */
    @NotBlank(message = "社团名称不能为空")
    @Size(max = 100, message = "社团名称不能超过100个字符")
    private String clubName;

    /**
     * 社团简介
     */
    @Size(max = 2000, message = "社团简介不能超过2000个字符")
    private String description;

    /**
     * 社团Logo
     */
    private String logoUrl;
}
