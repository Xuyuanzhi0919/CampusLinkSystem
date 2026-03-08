package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体类
 */
@Data
@TableName("task")
public class Task {
    /**
     * 任务ID
     */
    @TableId(value = "t_id", type = IdType.AUTO)
    private Long tid;

    /**
     * 发布者ID
     */
    @TableField("publisher_id")
    private Long publisherId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务内容
     */
    private String content;

    /**
     * 任务类型（errand-跑腿/borrow-借用/tutor-答疑互助/other-其他）
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 悬赏积分
     */
    @TableField("reward_points")
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
    @TableField("accepter_id")
    private Long accepterId;

    /**
     * 状态：0-待接单，1-进行中，2-已完成，3-已取消
     */
    private Integer status;

    /**
     * 图片URL列表（JSON数组）
     */
    private String images;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 执行者提交的结果描述
     */
    @TableField("result_description")
    private String resultDescription;

    /**
     * 结果附件URL列表
     */
    @TableField("result_attachments")
    private String resultAttachments;

    /**
     * 提交结果时间
     */
    @TableField("submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 发布者拒绝原因
     */
    @TableField("rejection_reason")
    private String rejectionReason;

    /**
     * 接单时间
     */
    @TableField("accepted_at")
    private LocalDateTime acceptedAt;

    /**
     * 开始执行时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;
}
