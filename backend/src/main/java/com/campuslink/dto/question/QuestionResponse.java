package com.campuslink.dto.question;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题详情响应
 */
@Data
public class QuestionResponse {
    /**
     * 问题ID
     */
    private Long qid;

    /**
     * 提问者ID
     */
    private Long askerId;

    /**
     * 提问者昵称
     */
    private String askerNickname;

    /**
     * 提问者头像
     */
    private String askerAvatar;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题内容
     */
    private String content;

    /**
     * 问题分类
     */
    private String category;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * AI智能答复
     */
    private String aiAnswer;

    /**
     * AI答复生成时间
     */
    private LocalDateTime aiGeneratedAt;

    /**
     * 悬赏积分
     */
    private Integer bounty;

    /**
     * 浏览次数
     */
    private Integer views;

    /**
     * 回答数量
     */
    private Integer answerCount;

    /**
     * 状态: 0-未解决, 1-已解决
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
