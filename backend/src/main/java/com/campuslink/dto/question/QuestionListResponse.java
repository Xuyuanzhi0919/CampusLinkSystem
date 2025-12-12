package com.campuslink.dto.question;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题列表项响应
 */
@Data
public class QuestionListResponse {
    /**
     * 问题ID
     */
    private Long qid;

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
     * 问题分类
     */
    private String category;

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
}
