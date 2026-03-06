package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源评论实体类
 */
@Data
@TableName("resource_comment")
public class ResourceComment {

    /**
     * 评论ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 资源ID
     */
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 评论者用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论ID（NULL表示一级评论，非NULL表示回复）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除标志（0=正常，1=已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
