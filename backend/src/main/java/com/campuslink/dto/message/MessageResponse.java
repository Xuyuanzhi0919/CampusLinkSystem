package com.campuslink.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信响应
 */
@Data
public class MessageResponse {
    /**
     * 消息ID
     */
    private Long mId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者昵称
     */
    private String receiverName;

    /**
     * 接收者头像
     */
    private String receiverAvatar;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-文本，2-图片，3-文件
     */
    private Integer msgType;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
