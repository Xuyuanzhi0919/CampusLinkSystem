package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@TableName("comment")
public class Comment {

    /**
     * 评论ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 评论者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论对象类型：resource/question/answer/activity等
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 评论对象ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID（用于回复）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 状态：0-已删除，1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
