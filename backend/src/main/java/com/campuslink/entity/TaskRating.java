package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务评价实体
 */
@Data
@TableName("task_rating")
public class TaskRating {
    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long ratingId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 评价者ID
     */
    private Long raterId;

    /**
     * 被评价者ID
     */
    private Long ratedId;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价标签(JSON格式)
     */
    private String tags;

    /**
     * 评价时间
     */
    private LocalDateTime createdAt;
}
