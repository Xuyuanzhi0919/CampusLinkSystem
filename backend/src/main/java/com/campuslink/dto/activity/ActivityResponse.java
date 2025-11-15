package com.campuslink.dto.activity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动响应
 */
@Data
public class ActivityResponse {
    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 社团ID
     */
    private Long clubId;

    /**
     * 社团名称
     */
    private String clubName;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants;

    /**
     * 当前参与人数
     */
    private Integer currentParticipants;

    /**
     * 剩余名额
     */
    private Integer remainingSlots;

    /**
     * 参与奖励积分
     */
    private Integer rewardPoints;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 状态：0-未开始，1-进行中，2-已结束，3-已取消
     */
    private Integer status;

    /**
     * 是否已报名
     */
    private Boolean isJoined;

    /**
     * 是否已签到
     */
    private Boolean isSignedIn;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
