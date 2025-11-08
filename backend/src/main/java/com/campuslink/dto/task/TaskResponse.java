package com.campuslink.dto.task;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务详情响应
 */
@Data
public class TaskResponse {
    /**
     * 任务ID
     */
    private Long tid;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 发布者昵称
     */
    private String publisherNickname;

    /**
     * 发布者头像
     */
    private String publisherAvatar;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务内容
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
     * 接单者ID
     */
    private Long accepterId;

    /**
     * 接单者昵称
     */
    private String accepterNickname;

    /**
     * 接单者头像
     */
    private String accepterAvatar;

    /**
     * 状态：0-待接单，1-进行中，2-已完成，3-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;
}
