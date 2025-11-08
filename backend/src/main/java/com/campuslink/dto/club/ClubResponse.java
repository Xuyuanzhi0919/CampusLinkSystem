package com.campuslink.dto.club;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社团响应
 */
@Data
public class ClubResponse {
    /**
     * 社团ID
     */
    private Long clubId;

    /**
     * 社团名称
     */
    private String clubName;

    /**
     * 社团简介
     */
    private String description;

    /**
     * 社团Logo
     */
    private String logoUrl;

    /**
     * 所属学校ID
     */
    private Long schoolId;

    /**
     * 创建者ID
     */
    private Long founderId;

    /**
     * 创建者昵称
     */
    private String founderName;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 状态：0-已解散，1-正常
     */
    private Integer status;

    /**
     * 当前用户角色（如果已加入）
     */
    private String userRole;

    /**
     * 是否已加入
     */
    private Boolean isMember;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
