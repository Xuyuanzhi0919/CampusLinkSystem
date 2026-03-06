package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动实体
 */
@Data
@TableName("activity")
public class Activity {
    /**
     * 活动ID
     */
    @TableId(value = "activity_id", type = IdType.AUTO)
    private Long activityId;

    /**
     * 社团ID（社团活动必填，其他类型可为空）
     */
    @TableField("club_id")
    private Long clubId;

    /**
     * 活动类型：club-社团活动, campus-校园活动, official-官方活动
     */
    @TableField("activity_type")
    private String activityType;

    /**
     * 组织者名称（非社团活动时使用，如"校学生会"、"教务处"）
     */
    @TableField("organizer_name")
    private String organizerName;

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
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 最大参与人数
     */
    @TableField("max_participants")
    private Integer maxParticipants;

    /**
     * 当前参与人数
     */
    @TableField("current_participants")
    private Integer currentParticipants;

    /**
     * 参与奖励积分
     */
    @TableField("reward_points")
    private Integer rewardPoints;

    /**
     * 封面图片
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 状态：0-未开始，1-进行中，2-已结束，3-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
