package com.campuslink.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表响应
 */
@Data
public class ConversationResponse {
    /**
     * 对方用户ID
     */
    private Long userId;

    /**
     * 对方用户昵称
     */
    private String nickname;

    /**
     * 对方用户头像
     */
    private String avatarUrl;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后一条消息类型
     */
    private Integer lastMessageType;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数量
     */
    private Integer unreadCount;
}
