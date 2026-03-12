package com.campuslink.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 封禁/解封用户请求
 */
@Data
public class BanUserRequest {
    /** 目标状态：0-禁用，1-正常 */
    @NotNull(message = "目标状态不能为空")
    private Integer status;
    /** 封禁原因（status=0 时必填） */
    private String reason;
}
