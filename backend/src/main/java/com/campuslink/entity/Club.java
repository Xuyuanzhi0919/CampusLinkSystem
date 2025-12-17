package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社团实体
 */
@Data
@TableName("club")
public class Club {
    /**
     * 社团ID
     */
    @TableId(value = "club_id", type = IdType.AUTO)
    private Long clubId;

    /**
     * 社团名称
     */
    @TableField("club_name")
    private String clubName;

    /**
     * 社团简介
     */
    private String description;

    /**
     * 社团Logo
     */
    @TableField("logo_url")
    private String logoUrl;

    /**
     * 所属学校ID
     */
    @TableField("school_id")
    private Long schoolId;

    /**
     * 创建者ID
     */
    @TableField("founder_id")
    private Long founderId;

    /**
     * 成员数量
     */
    @TableField("member_count")
    private Integer memberCount;

    /**
     * 状态：0-已解散，1-正常
     */
    private Integer status;

    /**
     * 是否官方/校级社团：0-否，1-是
     */
    @TableField("is_official")
    private Integer isOfficial;

    /**
     * 社团分类 (技术/学习/体育/艺术/公益/兴趣)
     */
    private String category;

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
