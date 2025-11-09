package com.campuslink.dto.resource;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核资源请求
 */
@Data
public class ReviewResourceRequest {
    /**
     * 拒绝原因（拒绝时必填）
     */
    @Size(max = 500, message = "拒绝原因不能超过500个字符")
    private String rejectReason;
}
