package com.campuslink.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应
 */
@Data
public class NotificationResponse {
    /**
     * 通知ID
     */
    private Long notificationId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     */
    private String notifyType;

    /**
     * 通知类型名称
     */
    private String notifyTypeName;

    /**
     * 关联对象类型
     */
    private String relatedType;

    /**
     * 关联对象ID
     */
    private Long relatedId;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
