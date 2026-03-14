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
     * 活动数量
     */
    private Integer activityCount;

    /**
     * 状态：0-已解散，1-正常，2-待审核，3-已拒绝
     */
    private Integer status;

    /**
     * 审核拒绝原因（status=3 时有值）
     */
    private String rejectReason;

    /**
     * 当前用户角色（如果已加入）
     */
    private String userRole;

    /**
     * 是否已加入
     */
    private Boolean isMember;

    /**
     * 是否有待审核的加入申请
     */
    private Boolean isPending;

    /**
     * 用户加入位置(第几位成员)
     * 仅当 isMember = true 时有值
     */
    private Integer joinPosition;

    /**
     * 是否官方/校级社团
     */
    private Boolean isOfficial;

    /**
     * 社团分类 (技术/学习/体育/艺术/公益/兴趣)
     */
    private String category;

    /**
     * 最近活动时间
     */
    private LocalDateTime lastActivityAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 学校名称
     */
    private String schoolName;
}
