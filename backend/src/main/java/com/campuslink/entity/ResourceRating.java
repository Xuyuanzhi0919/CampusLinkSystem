package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源评分实体
 */
@Data
@TableName("resource_rating")
public class ResourceRating {
    /**
     * 评分ID
     */
    @TableId(type = IdType.AUTO)
    private Long ratingId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 评分用户ID
     */
    private Long userId;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评分时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
