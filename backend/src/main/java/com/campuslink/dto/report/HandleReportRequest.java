package com.campuslink.dto.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处理举报请求
 */
@Data
public class HandleReportRequest {
    /**
     * 处理状态：1-已处理，2-已驳回
     */
    @NotNull(message = "处理状态不能为空")
    @Min(value = 1, message = "处理状态无效")
    @Max(value = 2, message = "处理状态无效")
    private Integer status;

    /**
     * 处理结果说明（可选）
     */
    private String handleResult;
}
