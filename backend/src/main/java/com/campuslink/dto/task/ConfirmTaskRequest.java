package com.campuslink.dto.task;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 确认任务完成请求
 */
@Data
public class ConfirmTaskRequest {
    /**
     * 是否批准(true-批准完成, false-拒绝需重新提交)
     */
    @NotNull(message = "批准状态不能为空")
    private Boolean approved;

    /**
     * 反馈意见(拒绝时必填)
     */
    @Size(max = 500, message = "反馈意见不能超过500字符")
    private String feedback;
}
