package com.campuslink.dto.task;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务列表项响应
 */
@Data
public class TaskListResponse {
    /**
     * 任务ID
     */
    private Long tid;

    /**
     * 发布者昵称
     */
    private String publisherNickname;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述摘要
     */
    private String content;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 悬赏积分
     */
    private Integer rewardPoints;

    /**
     * 地点
     */
    private String location;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 状态：0-待接单，1-进行中，2-已完成，3-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
