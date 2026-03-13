package com.campuslink.dto.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送通知请求(管理员)
 */
@Data
public class SendNotificationRequest {
    /**
     * 接收者ID(为null时发送给所有用户)
     */
    private Long userId;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 通知类型
     */
    @NotBlank(message = "通知类型不能为空")
    private String notifyType;

    /**
     * 按角色发送：student / teacher（userId 和 role 均为 null 时广播全体）
     */
    private String role;

    /**
     * 关联对象类型
     */
    private String relatedType;

    /**
     * 关联对象ID
     */
    private Long relatedId;
}
