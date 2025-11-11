package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@TableName("favorite")
public class Favorite {
    /**
     * 收藏ID
     */
    @TableId(type = IdType.AUTO)
    private Long favoriteId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏对象类型（task/resource/question等）
     */
    private String targetType;

    /**
     * 收藏对象ID
     */
    private Long targetId;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
}
