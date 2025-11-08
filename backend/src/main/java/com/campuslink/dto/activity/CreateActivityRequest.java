package com.campuslink.dto.activity;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 创建活动请求
 */
@Data
public class CreateActivityRequest {
    /**
     * 活动标题
     */
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 200, message = "活动标题不能超过200个字符")
    private String title;

    /**
     * 活动描述
     */
    @Size(max = 2000, message = "活动描述不能超过2000个字符")
    private String description;

    /**
     * 活动地点
     */
    @Size(max = 200, message = "活动地点不能超过200个字符")
    private String location;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 最大参与人数
     */
    @Min(value = 1, message = "最大参与人数至少为1")
    private Integer maxParticipants;

    /**
     * 参与奖励积分
     */
    @Min(value = 0, message = "奖励积分不能为负数")
    private Integer rewardPoints;

    /**
     * 封面图片
     */
    private String coverImage;
}
