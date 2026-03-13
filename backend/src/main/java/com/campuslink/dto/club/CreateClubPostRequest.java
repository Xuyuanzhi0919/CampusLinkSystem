package com.campuslink.dto.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发布社团动态请求
 */
@Data
public class CreateClubPostRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容不超过1000字")
    private String content;
}
