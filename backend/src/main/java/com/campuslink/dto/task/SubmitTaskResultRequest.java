package com.campuslink.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交任务结果请求
 */
@Data
public class SubmitTaskResultRequest {
    /**
     * 结果描述
     */
    @NotBlank(message = "结果描述不能为空")
    @Size(min = 10, max = 1000, message = "结果描述长度必须在10-1000字符之间")
    private String description;

    /**
     * 结果附件URL列表
     */
    private List<String> attachments;
}
