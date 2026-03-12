package com.campuslink.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新内容状态请求（审核通过/拒绝/下架）
 */
@Data
public class UpdateContentStatusRequest {
    /** 目标状态（依内容类型而定） */
    @NotNull(message = "状态不能为空")
    private Integer status;
    /** 操作原因/备注 */
    private String reason;
}
