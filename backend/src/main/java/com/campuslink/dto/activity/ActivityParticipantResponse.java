package com.campuslink.dto.activity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动参与者响应
 */
@Data
public class ActivityParticipantResponse {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 是否已签到
     */
    private Boolean isSignedIn;

    /**
     * 签到时间
     */
    private LocalDateTime signedInAt;

    /**
     * 报名时间
     */
    private LocalDateTime joinedAt;
}
