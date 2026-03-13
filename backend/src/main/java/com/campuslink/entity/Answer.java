package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答案实体类
 */
@Data
@TableName("answer")
public class Answer {
    /**
     * 答案ID
     */
    @TableId(value = "a_id", type = IdType.AUTO)
    private Long aid;

    /**
     * 问题ID
     */
    @TableField("question_id")
    private Long questionId;

    /**
     * 回答者ID
     */
    @TableField("responder_id")
    private Long responderId;

    /**
     * 答案内容
     */
    private String content;

    /**
     * 图片URL列表（JSON数组）
     */
    private String images;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 是否被采纳: 0-未采纳, 1-已采纳
     */
    @TableField("is_accepted")
    private Integer isAccepted;

    /**
     * 状态: 0-已删除, 1-正常
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

    /** 回答者昵称（非DB字段，管理端查询时填充） */
    @TableField(exist = false)
    private String responderName;

    /** 所属问题标题（非DB字段，管理端查询时填充） */
    @TableField(exist = false)
    private String questionTitle;
}
