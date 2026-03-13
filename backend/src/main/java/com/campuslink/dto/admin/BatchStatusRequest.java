package com.campuslink.dto.admin;

import lombok.Data;
import java.util.List;

/**
 * 批量修改用户状态请求
 */
@Data
public class BatchStatusRequest {
    /** 用户ID列表 */
    private List<Long> userIds;
    /** 目标状态：0-封禁，1-解封 */
    private Integer status;
    /** 操作原因（可选） */
    private String reason;
}
