package com.campuslink.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员编辑社团基本信息请求
 */
@Data
public class AdminUpdateClubInfoRequest {

    @NotBlank(message = "社团名称不能为空")
    @Size(max = 50, message = "社团名称最多 50 个字符")
    private String clubName;

    @Size(max = 30, message = "分类最多 30 个字符")
    private String category;

    @Size(max = 500, message = "简介最多 500 个字符")
    private String description;
}
