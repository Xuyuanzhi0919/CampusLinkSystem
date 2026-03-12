package com.campuslink.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员手动调整积分请求
 */
@Data
public class AdjustPointsRequest {
    /** 调整量（正数增加，负数扣减） */
    @NotNull(message = "调整量不能为空")
    private Integer delta;
    /** 调整原因 */
    private String reason;
}
