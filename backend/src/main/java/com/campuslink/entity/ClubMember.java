package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社团成员实体
 */
@Data
@TableName("club_member")
public class ClubMember {
    /**
     * 成员关系ID
     */
    @TableId(value = "cm_id", type = IdType.AUTO)
    private Long cmId;

    /**
     * 社团ID
     */
    @TableField("club_id")
    private Long clubId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色：member-成员，admin-管理员，founder-创始人
     */
    private String role;

    /**
     * 加入时间
     */
    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
