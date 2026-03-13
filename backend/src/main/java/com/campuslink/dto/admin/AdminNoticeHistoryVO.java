package com.campuslink.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端公告发送历史 VO
 */
@Data
public class AdminNoticeHistoryVO {

    private Long notificationId;

    /** 通知类型：announcement / system / warning */
    private String notifyType;

    private String title;
    private String content;
    private LocalDateTime createdAt;

    /** 该标题总接收人数（广播时 > 1） */
    private Integer recipientCount;

    // ─── 接收者信息（recipientCount == 1 时有效）─────────────
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
}
