package com.campuslink.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateClubRequest {

    @NotBlank(message = "社团名称不能为空")
    @Size(max = 100, message = "社团名称不超过100字符")
    private String clubName;

    @Size(max = 2000, message = "社团简介不超过2000字符")
    private String description;

    private String logoUrl;

    @Size(max = 20, message = "分类不超过20字符")
    private String category;
}
