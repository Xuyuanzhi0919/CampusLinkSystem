package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源点赞实体类
 */
@Data
@TableName("resource_like")
public class ResourceLike {
    @TableId(value = "like_id", type = IdType.AUTO)
    private Long likeId;

    @TableField("user_id")
    private Long userId;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
