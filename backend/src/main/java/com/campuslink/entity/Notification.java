package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@TableName("notification")
public class Notification {
    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

    /**
     * 接收者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型：system-系统通知，answer-回答通知，task-任务通知，activity-活动通知，message-私信通知
     */
    @TableField("notify_type")
    private String notifyType;

    /**
     * 关联对象类型
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 关联对象ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
