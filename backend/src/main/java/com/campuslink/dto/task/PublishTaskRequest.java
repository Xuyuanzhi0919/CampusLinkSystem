package com.campuslink.dto.task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发布任务请求
 */
@Data
public class PublishTaskRequest {
    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    @Size(min = 5, max = 200, message = "标题长度必须在5-200个字符之间")
    private String title;

    /**
     * 任务内容
     */
    @NotBlank(message = "任务内容不能为空")
    @Size(min = 10, max = 5000, message = "内容长度必须在10-5000个字符之间")
    private String content;

    /**
     * 任务类型
     */
    @NotBlank(message = "任务类型不能为空")
    @Pattern(regexp = "errand|borrow|tutor|other", message = "任务类型无效，支持：errand/borrow/tutor/other")
    private String taskType;

    /**
     * 悬赏积分
     */
    @NotNull(message = "悬赏积分不能为空")
    @Min(value = 1, message = "悬赏积分不能小于1")
    private Integer rewardPoints;

    /**
     * 地点（可选）
     */
    private String location;

    /**
     * 截止时间（可选）
     */
    private LocalDateTime deadline;
}
