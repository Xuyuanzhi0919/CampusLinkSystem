package com.campuslink.dto.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索关键词上报请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchReportRequest {
    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    @Size(max = 100, message = "搜索关键词最长100个字符")
    private String keyword;
}
