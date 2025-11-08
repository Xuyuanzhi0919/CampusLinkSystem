package com.campuslink.dto.club;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社团成员响应
 */
@Data
public class ClubMemberResponse {
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
     * 角色
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
}
