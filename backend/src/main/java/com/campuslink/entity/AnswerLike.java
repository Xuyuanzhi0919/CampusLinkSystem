package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答案点赞实体类
 */
@Data
@TableName("answer_like")
public class AnswerLike {
    @TableId(value = "like_id", type = IdType.AUTO)
    private Long likeId;

    @TableField("user_id")
    private Long userId;

    @TableField("answer_id")
    private Long answerId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
