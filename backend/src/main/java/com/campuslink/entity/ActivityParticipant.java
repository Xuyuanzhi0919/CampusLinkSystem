package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动参与实体
 */
@Data
@TableName("activity_participant")
public class ActivityParticipant {
    /**
     * 参与记录ID
     */
    @TableId(value = "ap_id", type = IdType.AUTO)
    private Long apId;

    /**
     * 活动ID
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 是否已签到：0-否，1-是
     */
    @TableField("is_signed_in")
    private Integer isSignedIn;

    /**
     * 签到时间
     */
    @TableField("signed_in_at")
    private LocalDateTime signedInAt;

    /**
     * 报名时间
     */
    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
