package com.campuslink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题实体类
 */
@Data
@TableName("question")
public class Question {
    /**
     * 问题ID
     */
    @TableId(value = "q_id", type = IdType.AUTO)
    private Long qid;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题内容
     */
    private String content;

    /**
     * 提问者ID
     */
    @TableField("asker_id")
    private Long askerId;

    /**
     * 问题分类 (学习/生活/技术等)
     */
    private String category;

    /**
     * 标签（JSON数组）
     */
    private String tags;

    /**
     * 图片URL列表（JSON数组）
     */
    private String images;

    /**
     * AI生成的答案
     */
    @TableField("ai_answer")
    private String aiAnswer;

    /**
     * AI答案生成时间
     */
    @TableField("ai_generated_at")
    private LocalDateTime aiGeneratedAt;

    /**
     * 浏览次数
     */
    private Integer views;

    /**
     * 回答数量
     */
    @TableField("answer_count")
    private Integer answerCount;

    /**
     * 是否已解决: 0-否, 1-是
     */
    @TableField("is_solved")
    private Integer isSolved;

    /**
     * 悬赏积分
     */
    @TableField("reward_points")
    private Integer rewardPoints;

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
}
